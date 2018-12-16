package br.edu.utfpr.dv.sireata.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import br.edu.utfpr.dv.sireata.model.AtaParticipante;

public class AtaParticipanteDAO {
	
	@FunctionName ("funcaoataparticipantebuscarporid")
	public AtaParticipante buscarPorId(
		@HttpTrigger (
			name = "restataparticipantebuscarporid",
			methods = {HttpMethod.PUT},
			route = "ataparticipante/{id}"
		)
		@BindingName ("id")
		int id) throws SQLException{

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.prepareStatement("SELECT ataparticipantes.*, usuarios.nome AS nomeParticipante FROM ataparticipantes " +
				"INNER JOIN usuarios ON usuarios.idUsuario=ataparticipantes.idUsuario " +
				"WHERE idAtaParticipante = ?");
		
			stmt.setInt(1, id);
			
			rs = stmt.executeQuery();
			
			if(rs.next()){
				return this.carregarObjeto(rs);
			}else{
				return null;
			}
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	@FunctionName ("funcaoataparticipantelistarporata")
	public List<AtaParticipante> listarPorAta(
		@HttpTrigger (
			name = "restataparticipantelistarporata",
			methods = {HttpMethod.GET},
			route = "ataparticipante/{idAta}"
		)
		@BindingName ("idAta")
		int idAta) throws SQLException{

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			rs = stmt.executeQuery("SELECT ataparticipantes.*, usuarios.nome AS nomeParticipante FROM ataparticipantes " +
				"INNER JOIN usuarios ON usuarios.idUsuario=ataparticipantes.idUsuario " + 
				"WHERE idAta=" + String.valueOf(idAta) + " ORDER BY usuarios.nome");
		
			List<AtaParticipante> list = new ArrayList<AtaParticipante>();
			
			while(rs.next()){
				list.add(this.carregarObjeto(rs));
			}
			
			return list;
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	@FunctionName ("funcaoataparticipantesalvar")
	public int salvar(
		@HttpTrigger (
			name = "restataparticipantesalvar",
			methods = {HttpMethod.POST},
			route = "ataparticipante"
		)
		AtaParticipante participante) throws SQLException{

		boolean insert = (participante.getIdAtaParticipante() == 0);
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
		
			if(insert){
				stmt = conn.prepareStatement("INSERT INTO ataparticipantes(idAta, idUsuario, presente, motivo, designacao) VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else{
				stmt = conn.prepareStatement("UPDATE ataparticipantes SET idAta=?, idUsuario=?, presente=?, motivo=?, designacao=? WHERE idAtaParticipante=?");
			}
			
			stmt.setInt(1, participante.getAta().getIdAta());
			stmt.setInt(2, participante.getParticipante().getIdUsuario());
			stmt.setInt(3, (participante.isPresente() ? 1 : 0));
			stmt.setString(4, participante.getMotivo());
			stmt.setString(5, participante.getDesignacao());
			
			if(!insert){
				stmt.setInt(6, participante.getIdAtaParticipante());
			}
			
			stmt.execute();
			
			if(insert){
				rs = stmt.getGeneratedKeys();
				
				if(rs.next()){
					participante.setIdAtaParticipante(rs.getInt(1));
				}
			}
			
			return participante.getIdAtaParticipante();
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	@FunctionName ("funcaoataparticipanteexcluir")
	public void excluir(
		@HttpTrigger (
			name = "restataparticipanteexcluir",
			methods = {HttpMethod.DELETE},
			route = "ataparticipante/{id}"
		)
		@BindingName ("id")
		int id) throws SQLException{
			
		Connection conn = null;
		Statement stmt = null;
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			stmt.execute("DELETE FROM ataparticipantes WHERE idAtaParticipante=" + String.valueOf(id));
		}finally{
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
	
	private AtaParticipante carregarObjeto(ResultSet rs) throws SQLException{
		AtaParticipante participante = new AtaParticipante();
		
		participante.setIdAtaParticipante(rs.getInt("idAtaParticipante"));
		participante.getAta().setIdAta(rs.getInt("idAta"));
		participante.getParticipante().setIdUsuario(rs.getInt("idUsuario"));
		participante.getParticipante().setNome(rs.getString("nomeParticipante"));
		participante.setPresente(rs.getInt("presente") == 1);
		participante.setMotivo(rs.getString("motivo"));
		participante.setDesignacao(rs.getString("designacao"));
		
		return participante;
	}

}
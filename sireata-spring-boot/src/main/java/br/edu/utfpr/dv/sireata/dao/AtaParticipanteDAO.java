package br.edu.utfpr.dv.sireata.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.dv.sireata.model.AtaParticipante;

@RestController
public class AtaParticipanteDAO {
	
	@PutMapping ("/ataparticipante/{id}")
	public AtaParticipante buscarPorId(@PathVariable int id) throws SQLException{
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
	
	@GetMapping ("/ataparticipante/{idAta}")
	public List<AtaParticipante> listarPorAta(@PathVariable int idAta) throws SQLException{
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
	
	@PostMapping ("/ataparticipante")
	public int salvar(@RequestBody AtaParticipante participante) throws SQLException{
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
	
	@DeleteMapping ("/ataparticipante/{id}")
	public void excluir(@PathVariable int id) throws SQLException{
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
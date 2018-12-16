package br.edu.utfpr.dv.sireata.dao.pauta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import br.edu.utfpr.dv.sireata.dao.ConnectionDAO;
import br.edu.utfpr.dv.sireata.model.Pauta;

public class PautaDAOSalvar {

	@FunctionName ("funcaopautasalvar")
    public int salvar(
		@HttpTrigger (
			name = "restpautasalvar",
			methods = {HttpMethod.POST},
			route = "pauta"
		)
		Pauta pauta) throws SQLException {
			
		boolean insert = (pauta.getIdPauta() == 0);
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
		
			if(insert){
				stmt = conn.prepareStatement("INSERT INTO pautas(idAta, ordem, titulo, descricao) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			}else{
				stmt = conn.prepareStatement("UPDATE pautas SET idAta=?, ordem=?, titulo=?, descricao=? WHERE idPauta=?");
			}
			
			stmt.setInt(1, pauta.getAta().getIdAta());
			stmt.setInt(2, pauta.getOrdem());
			stmt.setString(3, pauta.getTitulo());
			stmt.setString(4, pauta.getDescricao());
			
			if(!insert){
				stmt.setInt(5, pauta.getIdPauta());
			}
			
			stmt.execute();
			
			if(insert){
				rs = stmt.getGeneratedKeys();
				
				if(rs.next()){
					pauta.setIdPauta(rs.getInt(1));
				}
			}
			
			return pauta.getIdPauta();
		}finally{
			if((rs != null) && !rs.isClosed())
				rs.close();
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}

}
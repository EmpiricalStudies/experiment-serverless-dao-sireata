package br.edu.utfpr.dv.sireata.dao.pauta;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.dv.sireata.dao.ConnectionDAO;

@RestController
public class PautaDAOExcluir {
	
	@DeleteMapping ("/pauta/{id}")
    public void excluir(@PathVariable int id) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		
		try{
			conn = ConnectionDAO.getInstance().getConnection();
			stmt = conn.createStatement();
		
			stmt.execute("DELETE FROM pautas WHERE idPauta=" + String.valueOf(id));
		}finally{
			if((stmt != null) && !stmt.isClosed())
				stmt.close();
			if((conn != null) && !conn.isClosed())
				conn.close();
		}
	}
}
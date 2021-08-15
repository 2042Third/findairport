import java.util.prefs.*;
import java.sql.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FindAirports
 */
@WebServlet("/FindAirports")
public class FindAirports extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
	    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    String[] pairs = query.split("&");
	    for (String pair : pairs) {
	    	System.out.println(pair);
	        int idx = pair.indexOf("=");
	        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	    return query_pairs;
	}	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FindAirports() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("<html><title>Airport Search Web App</title><body>" +
				"<form method=\"POST\">Enter country:<br/><input name=\"country\" type=\"text\">" +
				"<br/>Enter city:<br/><input name=\"city\" type=\"text\" size=\"60\">" +
				"<br/><input type=\"submit\" value=\"Find\"></form>" +
				"</body></html>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws  IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("<html><title>Airport Search Web App</title><body>" +
				"<form method=\"POST\">Enter country:<br/><input name=\"country\" type=\"text\">" +
				"<br/>Enter city:<br/><input name=\"city\" type=\"text\" size=\"60\">" +
				"<br/><input type=\"submit\" value=\"Find\"></form>" +
				"</body></html>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//doGet(request, response);
        Scanner s = null;
        ServletInputStream inputStream = null;
        String data = "";
        try {
        	inputStream = request.getInputStream();
            s = new Scanner(inputStream, "UTF-8");
            s.useDelimiter("\\A");
            data = s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
        	if (s != null) {
        		s.close();
        	}
        }
        String country = "", city = "";
        if (!data.isEmpty()) {
			Map<String,String> parameterMap = splitQuery(data);
			System.out.println(data);
			if (parameterMap.containsKey("country")) {
				country = parameterMap.get("country");
			}
			if (parameterMap.containsKey("city")) {
				city = parameterMap.get("city");
			}
		}

		Preferences root  = Preferences.userRoot();
		Preferences node = Preferences.userNodeForPackage(this.getClass());
    String url = node.get("MySQLConnection", "jdbc:mysql://localhost:9234/advjava?useSSL=false");


		Connection con = null;
		StringBuffer resultTable = new StringBuffer(
				"<table><tr><th>Airport</th><th>City</th><th>Country</th>" +
				"<th>Latitude</th><th>Longitude</th></tr>"
				);
		try
		{
			con = DriverManager.getConnection(url, "admin", "f3ck");
			String query = "SELECT airport, city, country, latitude, longitude " + 
						  "FROM advjava.airports WHERE country = ? AND city = ?";
			try (PreparedStatement stat = con.prepareStatement(query)) {
				stat.setString(1, country);
				stat.setString(2, city);
				try (ResultSet rs = stat.executeQuery()) {
					System.out.println("Executed the following SQL statement:");
					System.out.println(query);
					while (rs.next()) {
						resultTable.append("<tr><td>").append(rs.getString(1)).
							append("</td><td>").append(rs.getString("city")).
							append("</td><td>").append(rs.getString(3)).
							append("</td><td>").append(rs.getDouble(4)).
							append("</td><td>").append(rs.getDouble(5)).
							append("</td></tr>");
					}
				}
				resultTable.append("</table>");
			}
		}
		catch (SQLException ex) {
			for (Throwable t : ex)
				System.out.println(t.getMessage());
			System.out.println("Opening connection unsuccessful!");
		}
		finally {
			node.put("MySQLConnection", url);
			if (con != null) {
				try {
					con.close();
				}
				catch (SQLException ex) {
					for (Throwable t : ex)
						System.out.println(t.getMessage());
					System.out.println("Closing connection unsuccessful!");
				}
			}
		}
        System.out.println("Country is: " + country);
		response.getWriter().append("<html><title>Airport Search Web App</title>" +
				"<head><style>\r\n" + 
				"table {\r\n" + 
				"  font-family: arial, sans-serif;\r\n" + 
				"  border-collapse: collapse;\r\n" + 
				"  width: 100%;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"td, th {\r\n" + 
				"  border: 1px solid #dddddd;\r\n" + 
				"  text-align: left;\r\n" + 
				"  padding: 8px;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"tr:nth-child(even) {\r\n" + 
				"  background-color: #dddddd;\r\n" + 
				"}\r\n" + 
				"</style></head>" +
				"<body>" +
				"<H1>Search results for country: " + country +
				" and city: " + city +
				"</H1>" +
				"" + resultTable.toString() + 
				"</body></html>");
	}

}

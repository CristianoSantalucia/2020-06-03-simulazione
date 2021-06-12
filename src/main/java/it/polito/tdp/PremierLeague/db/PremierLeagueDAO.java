package it.polito.tdp.PremierLeague.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.PremierLeague.model.Action;
import it.polito.tdp.PremierLeague.model.Adiacenza;
import it.polito.tdp.PremierLeague.model.Player;

public class PremierLeagueDAO
{

	public List<Player> listAllPlayers()
	{
		String sql = "SELECT * FROM Players";
		List<Player> result = new ArrayList<Player>();
		Connection conn = DBConnect.getConnection();

		try
		{
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next())
			{

				Player player = new Player(res.getInt("PlayerID"), res.getString("Name"));

				result.add(player);
			}
			conn.close();
			return result;

		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public List<Action> listAllActions()
	{
		String sql = "SELECT * FROM Actions";
		List<Action> result = new ArrayList<Action>();
		Connection conn = DBConnect.getConnection();

		try
		{
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next())
			{

				Action action = new Action(res.getInt("PlayerID"), res.getInt("MatchID"), res.getInt("TeamID"),
						res.getInt("Starts"), res.getInt("Goals"), res.getInt("TimePlayed"), res.getInt("RedCards"),
						res.getInt("YellowCards"), res.getInt("TotalSuccessfulPassesAll"),
						res.getInt("totalUnsuccessfulPassesAll"), res.getInt("Assists"),
						res.getInt("TotalFoulsConceded"), res.getInt("Offsides"));

				result.add(action);
			}
			conn.close();
			return result;

		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void getVertici(Map<Integer, Player> vertici, Double goals)
	{
		String sql = "SELECT p.*, AVG(a.Goals) AS goals "
				+ "FROM actions a, players p "
				+ "WHERE p.PlayerID = a.PlayerID "
				+ "GROUP BY p.PlayerID "
				+ "HAVING goals > ? "; 
		Connection conn = DBConnect.getConnection();
		try
		{
			PreparedStatement st = conn.prepareStatement(sql);
			st.setDouble(1, goals);
			ResultSet res = st.executeQuery();
			while (res.next())
			{
				Player player = new Player(res.getInt("PlayerID"), res.getString("Name"));
				vertici.putIfAbsent(player.getPlayerID(), player); 
			}
			conn.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public List<Adiacenza> getAdiacenze(Map<Integer, Player> vertici)
	{
		String sql = "SELECT a1.PlayerID id1, a2.PlayerID id2, SUM(a1.TimePlayed) - SUM(a2.TimePlayed) peso "
						+ "FROM actions a1, actions a2 "
						+ "WHERE a1.PlayerID < a2.PlayerID "
						+ "		AND a1.TeamID <> a2.TeamID "
						+ "		AND a1.MatchID = a2.MatchID "
						+ "		AND a1.`Starts` = 1 "
						+ "		AND a2.`Starts` = 1 "
						+ "GROUP BY id1,id2 "
						+ "HAVING peso <> 0 ";
		
		List<Adiacenza> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try
		{
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next())
			{
				int id1 = res.getInt("id1"); 
				int id2 = res.getInt("id2"); 
				Player p1 = vertici.get(id1); 
				Player p2 = vertici.get(id2); 
				if (p1 != null && p2 != null)
				{
					Adiacenza a = new Adiacenza(p1, p2, res.getDouble("peso")); 
					result.add(a);
				}
			}
			conn.close();
			return result;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}

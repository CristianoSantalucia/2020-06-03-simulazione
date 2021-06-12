package it.polito.tdp.PremierLeague.model;

import java.util.*;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.*;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model
{
	private PremierLeagueDAO dao;
	private Map<Integer, Player> vertici;
	private Graph<Player, DefaultWeightedEdge> grafo;

	public Model()
	{
		this.dao = new PremierLeagueDAO();
	}

	public void creaGrafo(Double goals)
	{
		// ripulisco mappa e grafo
		this.vertici = new HashMap<>();
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class); //

		/// vertici
		this.dao.getVertici(vertici, goals); // riempio la mappa
		Graphs.addAllVertices(this.grafo, this.vertici.values());

		/// archi
		List<Adiacenza> adiacenze = new ArrayList<>(this.dao.getAdiacenze(this.vertici));
		for (Adiacenza a : adiacenze)
		{
			Player n1 = a.getP1();
			Player n2 = a.getP2();
			Double dif = a.getPeso();
			if (n1 != null && n2 != null) 
			{
				if(dif>0)
					Graphs.addEdge(this.grafo, n1, n2, Math.abs(dif));
				else 
					Graphs.addEdge(this.grafo, n2, n1, Math.abs(dif));
			}
		}
	}

	public Player getBestPlayer()
	{
		double bestPeso = 0; 
		Player bestPlayer = null; 
		for (Player p : this.grafo.vertexSet())
		{
			double uscenti = 0; 
			for (DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(p))
				uscenti += this.grafo.getEdgeWeight(e);
			if(uscenti > bestPeso)
			{
				bestPeso = uscenti; 
				bestPlayer = p; 
			}
		}
		return bestPlayer; 
	}

	public String stampaBattutiDa(Player p)
	{
		String s = "Giocatori battuti da " + p + ":"; 
		for (DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(p))
			s += "\n" + this.grafo.getEdgeTarget(e) + ";"; 
		return s; 
	}

	public int getNumVertici()
	{
		return this.grafo.vertexSet().size();
	}
	public int getNumArchi()
	{
		return this.grafo.edgeSet().size();
	}
	public Collection<Player> getVertici()
	{
		return this.grafo.vertexSet();
	}
	public Collection<DefaultWeightedEdge> getArchi()
	{
		return this.grafo.edgeSet();
	}

	List<Player> dreamTeam;
	List<Player> disponibili;
	double bestValore = 0;
	public List<Player> team(int k)
	{
		dreamTeam = new ArrayList<>();
		disponibili = new ArrayList<>(this.grafo.vertexSet());
		List<Player> parziale = new ArrayList<>();
		this.cerca(parziale, k, disponibili);
		System.out.println(dreamTeam);
		return dreamTeam;
	}

	private void cerca(List<Player> parziale, int k, List<Player> disponibili)
	{
		//terminale
		if(parziale.size() == k)
		{
			double valore = 0;
			for(Player p : parziale)
				valore += this.grado(p);
			if(valore > bestValore)
			{
				bestValore = valore;
				dreamTeam = new ArrayList<>(parziale);
			}
			return; 
		}
		//ricorsivo
		{
			ArrayList<Player> disp = new ArrayList<>(disponibili);
			for(Player p : disp)
			{
				parziale.add(p); 
				disponibili.remove(p); 
				for (DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(p))
					disponibili.remove(this.grafo.getEdgeTarget(e));
				
				cerca(parziale, k, disponibili);
				
				for (DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(p))
					disponibili.add(this.grafo.getEdgeTarget(e));
				disponibili.add(p); 
				parziale.remove(p); 
			}
		}
	}
	public void getBattutiDa(Player p)
	{
		
	}
	public double grado(Player p)
	{ 
		double entranti = 0; 
		double uscenti = 0; 
		for (DefaultWeightedEdge e : this.grafo.incomingEdgesOf(p))
			entranti += this.grafo.getEdgeWeight(e);
		for (DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(p))
			uscenti += this.grafo.getEdgeWeight(e);
		return uscenti-entranti;
	}
}
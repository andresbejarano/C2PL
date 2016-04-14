package waitforgraph;

/*
 * JBoss, Home of Professional Open Source Copyright 2006, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

import java.util.ArrayList;
import java.util.List;

public class WaitForGraph {
	
	/* The tag name of the class */
	public static final String TAG = WaitForGraph.class.getName();
	
	/**/
	private Graph<Integer> wfg;
	
	/**
	 * 
	 */
	public WaitForGraph() {
		wfg = new Graph<Integer>();
	}
	
	/**
	 * 
	 * @param trans1
	 * @param trans2
	 * @return
	 */
	public Boolean addDependency(Integer trans1, Integer trans2) {
		Vertex<Integer> t1 = wfg.findVertexByName(trans1.toString());
		Vertex<Integer> t2 = wfg.findVertexByName(trans2.toString());
		
		if(t1 == null)
		{
			t1 = new Vertex<Integer>(trans1.toString(), trans1);
			wfg.addVertex(t1);
		}
		if(t2 == null)
		{
			t2 = new Vertex<Integer>(trans2.toString(), trans2);
			wfg.addVertex(t2);
		}
		
		return wfg.addEdge(t1, t2, 1);
	}
	
	/**
	 * 
	 * @param trans
	 * @return
	 */
	public Boolean addTransaction(Integer trans) {
		if(this.containsTransaction(trans))
			return false;
		
		Vertex<Integer> v = new Vertex<Integer>(trans.toString(), trans);
		wfg.addVertex(v);
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Integer> checkCycles() {
		Edge<Integer>[] edgesToRemove = wfg.findCycles();
		
		if(edgesToRemove.length == 0)
			return null;
		
		List<Integer> edge = new ArrayList<Integer>();
		edge.add(edgesToRemove[0].getFrom().getData());
		edge.add(edgesToRemove[0].getTo().getData());
		
		return edge;
	}
	
	/**
	 * 
	 * @param trans
	 * @return
	 */
	public Boolean containsTransaction(Integer trans) {
		return (wfg.findVertexByName(trans.toString()) != null);
	}
	
}

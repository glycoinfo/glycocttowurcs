package org.eurocarbdb.MolecularFramework.io.WURCS;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
 * Class of GlycoGraph traverser for WURCS
 * @author MasaakiMatsubara
 *
 */
public class WURCSTraverser extends GlycoTraverser
{
	public WURCSTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
	{
		super(a_objVisitor);
	}

	public void traverse(GlycoEdge a_objEdge) throws GlycoVisitorException
	{
        // callback of the function before subtree
        this.m_iState = GlycoTraverser.ENTER;
        a_objEdge.accept(this.m_objVisitor);
        // traverse subtree
        this.traverse(a_objEdge.getChild());
	}


	/**
	 * @see org.glycomedb.MolecularFrameWork.util.traverser.SugarTraverser#traverse(org.eurocarbdb.MolecularFramework.sugar.GlycoNode)
	 */
	@Override
	public void traverse(GlycoNode a_objNode) throws GlycoVisitorException
	{
    	// callback before subtree
    	this.m_iState = GlycoTraverser.ENTER;
    	a_objNode.accept(this.m_objVisitor);
    	// traverse subtree in GlycoCT order
    	ArrayList <GlycoEdge> t_aEdge = a_objNode.getChildEdges();
    	WURCSGlycoEdgeComparator t_oNodeComparator = new WURCSGlycoEdgeComparator();
    	Collections.sort(t_aEdge,t_oNodeComparator);
    	for (GlycoEdge t_oEdge : t_aEdge)
    	{
    		this.traverse(t_oEdge);
    	}
    	// callback after subtree
//    	this.m_iState = GlycoTraverser.LEAVE;
//    	a_objNode.accept(this.m_objVisitor);
	}

	/**
	 * @see org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser#traverseGraph(org.eurocarbdb.MolecularFramework.sugar.GlycoGraph)
	 */
	@Override
	public void traverseGraph(GlycoGraph a_objSugar) throws GlycoVisitorException
	{
		ArrayList<GlycoNode> t_aRoot;
        try
        {
        	// get root nodes of forest of graphs
            t_aRoot = a_objSugar.getRootNodes();
            //priorize according to WURCS all isolated subgraphs and process consecutivly
            WURCSGlycoNodeComparator t_oNodeComparator = new WURCSGlycoNodeComparator();
			Collections.sort(t_aRoot,t_oNodeComparator);

            Iterator<GlycoNode> t_objIterator = t_aRoot.iterator();
            while ( t_objIterator.hasNext() )
            {
                this.traverse(t_objIterator.next());
            }
        }
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
	}
}


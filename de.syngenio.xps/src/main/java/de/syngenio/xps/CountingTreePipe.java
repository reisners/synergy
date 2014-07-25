package de.syngenio.xps;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.sideeffect.SideEffectPipe;

import de.syngenio.xps.GraphAnalyzer.CountingTree;

public class CountingTreePipe<S> extends AbstractPipe<S, S> implements SideEffectPipe.GreedySideEffectPipe<S, GraphAnalyzer.CountingTree<Object>> {

    GraphAnalyzer.CountingTree<Object> tree = new GraphAnalyzer.CountingTree<Object>();
    final List<PipeFunction> branchFunctions;
    int currentFunction = 0;

    public CountingTreePipe(final PipeFunction... branchFunctions) {
        if (branchFunctions.length == 0)
            this.branchFunctions = null;
        else
            this.branchFunctions = Arrays.asList(branchFunctions);
    }

    public CountingTreePipe(final GraphAnalyzer.CountingTree tree, final PipeFunction... branchFunctions) {
        this(branchFunctions);
        this.tree = tree;
    }

    public void setStarts(Iterator<S> starts) {
        super.setStarts(starts);
        this.enablePath(true);
    }

    public S processNextStart() {
        final S s = this.starts.next();
        final List path = ((Pipe) this.starts).getCurrentPath();
        CountingTree<Object> depth = this.tree;
        for (int i = 0; i < path.size(); i++) {
            Object object = path.get(i);
            if (null != this.branchFunctions) {
                object = this.branchFunctions.get(this.currentFunction).compute(object);
                this.currentFunction = (this.currentFunction + 1) % this.branchFunctions.size();
            }

            if (!depth.containsKey(object))
                depth.put(object, new CountingTree<Object>());

            depth = depth.get(object);
            depth.getCounter().incrementAndGet();
        }
        return s;
    }

    public CountingTree<Object> getSideEffect() {
        return this.tree;
    }

    public void reset() {
        this.tree = new GraphAnalyzer.CountingTree<Object>();
        this.currentFunction = 0;
        super.reset();
    }


}

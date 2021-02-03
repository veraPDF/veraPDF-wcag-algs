package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.semanticalgorithms.mapper.AccumulatedNodeMapper;

import java.util.function.Consumer;

public class AccumulatedNodeConsumer implements Consumer<INode> {

	private final AccumulatedNodeMapper accumulatedNodeMapper;

	public AccumulatedNodeConsumer() {
		this.accumulatedNodeMapper = new AccumulatedNodeMapper();
	}

	@Override
	public void accept(INode node) {
		accumulatedNodeMapper.calculateSemanticScore(node);
	}
}



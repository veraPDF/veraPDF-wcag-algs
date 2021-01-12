package org.verapdf.wcag.algorithms.entity;

// make extends Collection<E>
public interface ITree<E> extends Iterable<E> {
	E getRoot();
}
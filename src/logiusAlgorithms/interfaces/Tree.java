package logiusAlgorithms.interfaces;

// make extends Collection<E>
public interface Tree<E> extends Iterable<E> {
    E getRoot();
}
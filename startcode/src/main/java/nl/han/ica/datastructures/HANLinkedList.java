package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private Node<T> header;
    private int size;

    public HANLinkedList() {
        this.header = new Node<>(null);
        this.size = 0;
    }

    @Override
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);
        newNode.next = header.next;
        header.next = newNode;
        size++;
    }
    @Override
    public void clear() {
        header.next = null;
        size = 0;
    }
    @Override
    public void insert(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> current = header;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        Node<T> newNode = new Node<>(value);
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }
    @Override
    public void delete(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> current = header;
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }

        if (current.next != null) {
            current.next = current.next.next;
            size--;
        }
    }
    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> current = header.next;
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }

        return current.value;
    }
    @Override
    public void removeFirst() {
        if (header.next != null) {
            header.next = header.next.next;
            size--;
        }
    }
    @Override
    public T getFirst() {
        if (header.next == null) {
            return null;
        }
        return header.next.value;
    }
    @Override
    public int getSize() {
        return size;
    }

    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
            this.next = null;
        }
    }
}
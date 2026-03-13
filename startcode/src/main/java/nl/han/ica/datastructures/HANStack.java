package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class HANStack<T> implements IHANStack<T> {

    private List<T> list;

    public HANStack() {
        this.list = new ArrayList<>();
    }

    @Override
    public void push(T value) {
        list.add(value);
    }

    @Override
    public T pop() {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }

        return list.remove(list.size() - 1);
    }

    @Override
    public T peek() {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }

        return list.get(list.size() - 1);
    }
}
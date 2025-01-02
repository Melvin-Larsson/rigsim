package org.example;

import java.util.Iterator;
import java.util.List;

public class ImmutableList<T> implements Iterable<T>{
    private List<T> list;

    public ImmutableList(List<T> list){
        this.list = list;
    }

    public T get(int index){
        return list.get(index);
    }

    public int size(){
        return list.size();
    }

    public boolean isEmpty(){
        return list.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
}

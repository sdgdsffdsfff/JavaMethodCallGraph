package com.se.utils;

import java.util.Objects;

public class Pair<F, S> {
    protected final F first;    // pair中的第一个元素
    protected final S second;   // pair中的第二个元素

    public static <A, B> Pair <A, B> create(A a, B b)
    {
        return new Pair<>(a, b);
    }

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public String toString()
    {
        return "[First = " + first + ", Second = " + second + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair))
            return false;

        Pair<?, ?> p = (Pair<?, ?>)obj;

        return (Objects.equals(p.first, this.first) && Objects.equals(p.second, this.second)) ||
               (Objects.equals(p.first, this.second) && Objects.equals(p.second, this.first));
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^
               (second == null ? 0 : second.hashCode());
    }
}

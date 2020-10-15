package com.se.utils;

import java.util.*;

public class TreeNode<N> {
    protected TreeNode<N> parent;
    protected List<TreeNode<N>> children;
    protected N value;

    public TreeNode(N value) {
        this.parent = null;
        this.children = new ArrayList<>();
        this.value = value;
    }

    public boolean forgetParent() {
        if(this.parent != null)
            return parent.removeChild(this);
        return true;
    }

    public boolean removeChild(TreeNode<N> child) {
        if (this.children.contains(child)) {
            child.parent = null;
            return this.children.remove(child);
        }

        return false;
    }

    public TreeNode<N> removeChildAt(int index) throws IndexOutOfBoundsException {
        return this.children.remove(index);
    }

    public void removeChildren() {
        for(TreeNode<N> child : this.children)
            child.parent = null;

        this.children.clear();
    }

    public boolean addChildNode(TreeNode<N> child) {
        if (!children.contains(child))
        {
            child.forgetParent();

            child.parent = this;
            return children.add(child);
        }

        return false;
    }

    public TreeNode<N> deepCopy() {
        TreeNode<N> newNode = new TreeNode(this.value);

        for (TreeNode<N> child : this.children)
            newNode.addChildNode(child.deepCopy());

        return newNode;
    }

    public int getLevel() {
        int level = 0;
        TreeNode p = this.parent;

        while(p != null)
        {
            ++level;
            p = p.parent;
        }

        return level;
    }

    public List<TreeNode<N>> getChildren() {
        return this.children;
    }

    public List<TreeNode<N>> getLeaves() {
        List<TreeNode<N>> leaves = new ArrayList<>();

        DepthFirstIterator it = this.new DepthFirstIterator();
        while (it.hasNext())
        {
            TreeNode<N> node = it.next();
            if(node.isLeaf())
                leaves.add(node);
        }

        return leaves;
    }

    public int childrenCount()
    {
        return this.children.size();
    }

    public TreeNode<N> getParent()
    {
        return this.parent;
    }

    public N getValue()
    {
        return this.value;
    }

    public void setValue(N value)
    {
        this.value = value;
    }

    public boolean isLeaf()
    {
        return this.children.isEmpty();
    }

    public class DepthFirstIterator implements Iterator<TreeNode<N>> {
        protected Stack<TreeNode<N>> fringe;

        public DepthFirstIterator() {
            this.fringe = new Stack<>();
            fringe.push(TreeNode.this);
        }

        @Override
        public boolean hasNext()
        {
            return !this.fringe.isEmpty();
        }

        @Override
        public TreeNode<N> next() {
            if(!this.hasNext())
              throw new NoSuchElementException("Tree ran out of elements");

            TreeNode<N> node = fringe.pop();

            for(TreeNode<N> childNode : node.children)
                this.fringe.push(childNode);

            return node;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

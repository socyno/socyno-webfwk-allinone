package com.weimob.webfwk.util.vcs.svn;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@ToString
@Accessors(chain=true)
public class SubversionCommitNode {
    
    private String name;
    
    private byte[] data;
    
    private byte[] originData;
    
    private List<SubversionCommitNode> children;
    
    private SubversionEntryOpType entryOpType = SubversionEntryOpType.ADD;
    
    private boolean pathExists = false;
    
    public boolean isDirectory() {
        return data == null;
    }
    
    public synchronized SubversionCommitNode addChild(@NonNull SubversionCommitNode child) {
        if (children == null) {
            children = new ArrayList<SubversionCommitNode>();
        }
        children.add(child);
        return this;
    }
    
    public synchronized SubversionCommitNode addChildren(List<SubversionCommitNode> nodes) {
        if (nodes == null) {
            return this;
        }
        if (children == null) {
            children = new ArrayList<SubversionCommitNode>();
        }
        for (SubversionCommitNode n : nodes) {
            if (n == null) {
                continue;
            }
            children.add(n);
        }
        return this;
    }

    public boolean noChildren() {
        return children == null || children.isEmpty();
    }
}


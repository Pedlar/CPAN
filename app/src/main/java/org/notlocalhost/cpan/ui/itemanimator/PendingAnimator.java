package org.notlocalhost.cpan.ui.itemanimator;

import android.support.v7.widget.RecyclerView;

abstract class PendingAnimator {
    RecyclerView.ViewHolder viewHolder;

    public PendingAnimator(RecyclerView.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }
    abstract void animate(BaseItemAnimator.OnAnimatorEnd callback);
    abstract void cancel();

    public static abstract class Add extends PendingAnimator {
        public Add(RecyclerView.ViewHolder viewHolder) {
            super(viewHolder);
        }
    }

    public static abstract class Remove extends PendingAnimator {
        public Remove(RecyclerView.ViewHolder viewHolder) {
            super(viewHolder);
        }
    }

    public static abstract class Move extends PendingAnimator {
        public Move(RecyclerView.ViewHolder viewHolder) {
            super(viewHolder);
        }
    }
}
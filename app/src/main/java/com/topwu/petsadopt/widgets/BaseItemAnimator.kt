package com.topwu.petsadopt.widgets

import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.topwu.petsadopt.util.ViewHelper
import java.util.ArrayList

abstract class BaseItemAnimator : SimpleItemAnimator() {
    private val DEBUG = false

    private val pendingRemovals = ArrayList<ViewHolder>()
    private val pendingAdditions = ArrayList<ViewHolder>()
    private val pendingMoves = ArrayList<MoveInfo>()
    private val pendingChanges = ArrayList<ChangeInfo>()

    private val additionsList = ArrayList<ArrayList<ViewHolder>>()
    private val movesList = ArrayList<ArrayList<MoveInfo>>()
    private val changesList = ArrayList<ArrayList<ChangeInfo>>()

    private val addAnimations = ArrayList<ViewHolder>()
    private val moveAnimations = ArrayList<ViewHolder>()
    private val removeAnimations = ArrayList<ViewHolder>()
    private val changeAnimations = ArrayList<ViewHolder>()

    protected var interpolator: Interpolator = LinearInterpolator()

    private data class MoveInfo(val holder: ViewHolder,
                                val fromX: Int,
                                val fromY: Int,
                                val toX: Int,
                                val toY: Int)
    private data class ChangeInfo(val oldHolder: ViewHolder,
                                  val newHolder: ViewHolder,
                                  val fromX: Int = 0,
                                  val fromY: Int = 0,
                                  val toX: Int = 0,
                                  val toY:Int = 0) {
        override fun toString(): String {
            return "ChangeInfo{oldHolder=$oldHolder, newHolder=$newHolder, fromX=$fromX, fromY=$fromY, toX=$toX, toY=$toY}"
        }
    }

    init {
        supportsChangeAnimations = false
    }

    override fun runPendingAnimations() {
        val removalsPending = !pendingRemovals.isEmpty()
        val movesPending = !pendingMoves.isEmpty()
        val changesPending = !pendingChanges.isEmpty()
        val additionsPending = !pendingAdditions.isEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return
        }
        // First, remove stuff
        for (holder in pendingRemovals) {
            doAnimateRemove(holder)
        }
        pendingRemovals.clear()
        // Next, move stuff
        if (movesPending) {
            val moves = ArrayList<MoveInfo>()
            moves.addAll(pendingMoves)
            movesList.add(moves)
            pendingMoves.clear()
            val mover = Runnable {
                for ((holder, fromX, fromY, toX, toY) in moves) {
                    animateMoveImpl(holder, fromX, fromY, toX, toY)
                }
                moves.clear()
                movesList.remove(moves)
            }
            if (removalsPending) {
                val view = moves[0].holder.itemView
                ViewCompat.postOnAnimationDelayed(view, mover, removeDuration)
            } else {
                mover.run()
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            val changes = ArrayList<ChangeInfo>()
            changes.addAll(pendingChanges)
            changesList.add(changes)
            pendingChanges.clear()
            val changer = Runnable {
                for (change in changes) {
                    animateChangeImpl(change)
                }
                changes.clear()
                changesList.remove(changes)
            }
            if (removalsPending) {
                val holder = changes[0].oldHolder
                ViewCompat.postOnAnimationDelayed(holder.itemView, changer, removeDuration)
            } else {
                changer.run()
            }
        }
        // Next, add stuff
        if (additionsPending) {
            val additions = ArrayList<ViewHolder>()
            additions.addAll(pendingAdditions)
            additionsList.add(additions)
            pendingAdditions.clear()
            val adder = Runnable {
                for (holder in additions) {
                    doAnimateAdd(holder)
                }
                additions.clear()
                additionsList.remove(additions)
            }
            if (removalsPending || movesPending || changesPending) {
                val removeDuration = if (removalsPending) removeDuration else 0
                val moveDuration = if (movesPending) moveDuration else 0
                val changeDuration = if (changesPending) changeDuration else 0
                val totalDelay = removeDuration + Math.max(moveDuration, changeDuration)
                val view = additions[0].itemView
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay)
            } else {
                adder.run()
            }
        }
    }

    protected fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder) {}

    protected open fun preAnimateAddImpl(holder: RecyclerView.ViewHolder) {}

    protected abstract fun animateRemoveImpl(holder: RecyclerView.ViewHolder)

    protected abstract fun animateAddImpl(holder: RecyclerView.ViewHolder)

    private fun preAnimateRemove(holder: RecyclerView.ViewHolder) {
        ViewHelper.clear(holder.itemView)

        if (holder is AnimateViewHolder) {
            holder.preAnimateRemoveImpl()
        } else {
            preAnimateRemoveImpl(holder)
        }
    }

    private fun preAnimateAdd(holder: RecyclerView.ViewHolder) {
        ViewHelper.clear(holder.itemView)

        if (holder is AnimateViewHolder) {
            holder.preAnimateAddImpl()
        } else {
            preAnimateAddImpl(holder)
        }
    }

    private fun doAnimateRemove(holder: RecyclerView.ViewHolder) {
        if (holder is AnimateViewHolder) {
            holder.animateRemoveImpl(DefaultRemoveVpaListener(holder))
        } else {
            animateRemoveImpl(holder)
        }

        removeAnimations.add(holder)
    }

    private fun doAnimateAdd(holder: RecyclerView.ViewHolder) {
        if (holder is AnimateViewHolder) {
            holder.animateAddImpl(DefaultAddVpaListener(holder))
        } else {
            animateAddImpl(holder)
        }

        addAnimations.add(holder)
    }

    override fun animateRemove(holder: ViewHolder): Boolean {
        endAnimation(holder)
        preAnimateRemove(holder)
        pendingRemovals.add(holder)
        return true
    }

    protected fun getRemoveDelay(holder: RecyclerView.ViewHolder): Long {
        return Math.abs(holder.oldPosition * removeDuration / 4)
    }

    override fun animateAdd(holder: ViewHolder): Boolean {
        endAnimation(holder)
        preAnimateAdd(holder)
        pendingAdditions.add(holder)
        return true
    }

    protected fun getAddDelay(holder: RecyclerView.ViewHolder): Long {
        return Math.abs(holder.adapterPosition * addDuration / 4)
    }

    override fun animateMove(holder: ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        var fromX = fromX
        var fromY = fromY
        val view = holder.itemView
        fromX += ViewCompat.getTranslationX(holder.itemView).toInt()
        fromY += ViewCompat.getTranslationY(holder.itemView).toInt()
        endAnimation(holder)
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        if (deltaX != 0) {
            ViewCompat.setTranslationX(view, (-deltaX).toFloat())
        }
        if (deltaY != 0) {
            ViewCompat.setTranslationY(view, (-deltaY).toFloat())
        }
        pendingMoves.add(MoveInfo(holder, fromX, fromY, toX, toY))
        return true
    }

    private fun animateMoveImpl(holder: ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0f)
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0f)
        }
        // TODO: make EndActions end listeners instead, since end actions aren't called when
        // vpas are canceled (and can't end them. why?)
        // need listener functionality in VPACompat for this. Ick.
        moveAnimations.add(holder)
        val animation = ViewCompat.animate(view)
        animation.setDuration(moveDuration).setListener(object : VpaListenerAdapter() {
            override fun onAnimationStart(view: View) {
                dispatchMoveStarting(holder)
            }

            override fun onAnimationCancel(view: View) {
                if (deltaX != 0) {
                    ViewCompat.setTranslationX(view, 0f)
                }
                if (deltaY != 0) {
                    ViewCompat.setTranslationY(view, 0f)
                }
            }

            override fun onAnimationEnd(view: View) {
                animation.setListener(null)
                dispatchMoveFinished(holder)
                moveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }

    override fun animateChange(oldHolder: ViewHolder, newHolder: ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        val prevTranslationX = ViewCompat.getTranslationX(oldHolder.itemView)
        val prevTranslationY = ViewCompat.getTranslationY(oldHolder.itemView)
        val prevAlpha = ViewCompat.getAlpha(oldHolder.itemView)
        endAnimation(oldHolder)
        val deltaX = (toX.toFloat() - fromX.toFloat() - prevTranslationX).toInt()
        val deltaY = (toY.toFloat() - fromY.toFloat() - prevTranslationY).toInt()
        // recover prev translation state after ending animation
        ViewCompat.setTranslationX(oldHolder.itemView, prevTranslationX)
        ViewCompat.setTranslationY(oldHolder.itemView, prevTranslationY)
        ViewCompat.setAlpha(oldHolder.itemView, prevAlpha)
        if (newHolder.itemView != null) {
            // carry over translation values
            endAnimation(newHolder)
            ViewCompat.setTranslationX(newHolder.itemView, (-deltaX).toFloat())
            ViewCompat.setTranslationY(newHolder.itemView, (-deltaY).toFloat())
            ViewCompat.setAlpha(newHolder.itemView, 0f)
        }
        pendingChanges.add(ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY))
        return true
    }

    private fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder.itemView
        if (view != null) {
            changeAnimations.add(changeInfo.oldHolder)
            val oldViewAnim = ViewCompat.animate(view).setDuration(changeDuration)
            oldViewAnim.translationX((changeInfo.toX - changeInfo.fromX).toFloat())
            oldViewAnim.translationY((changeInfo.toY - changeInfo.fromY).toFloat())
            oldViewAnim.alpha(0f).setListener(object : VpaListenerAdapter() {
                override fun onAnimationStart(view: View) {
                    dispatchChangeStarting(changeInfo.oldHolder, true)
                }

                override fun onAnimationEnd(view: View) {
                    oldViewAnim.setListener(null)
                    ViewCompat.setAlpha(view, 1f)
                    ViewCompat.setTranslationX(view, 0f)
                    ViewCompat.setTranslationY(view, 0f)
                    dispatchChangeFinished(changeInfo.oldHolder, true)
                    changeAnimations.remove(changeInfo.oldHolder)
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
        if (newView != null) {
            changeAnimations.add(changeInfo.newHolder)
            val newViewAnimation = ViewCompat.animate(newView)
            newViewAnimation.translationX(0f).translationY(0f).setDuration(changeDuration).alpha(1f).setListener(object : VpaListenerAdapter() {
                override fun onAnimationStart(view: View) {
                    dispatchChangeStarting(changeInfo.newHolder, false)
                }

                override fun onAnimationEnd(view: View) {
                    newViewAnimation.setListener(null)
                    ViewCompat.setAlpha(newView, 1f)
                    ViewCompat.setTranslationX(newView, 0f)
                    ViewCompat.setTranslationY(newView, 0f)
                    dispatchChangeFinished(changeInfo.newHolder, false)
                    changeAnimations.remove(changeInfo.newHolder)
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
    }

    private fun endChangeAnimation(infoList: MutableList<ChangeInfo>, item: ViewHolder) {
        infoList.removeAll {
            endChangeAnimationIfNecessary(it, item)
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder)
        endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder)
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo, item: ViewHolder): Boolean {
        var oldItem = false
        if (changeInfo.newHolder === item) {
        } else if (changeInfo.oldHolder === item) {
            oldItem = true
        } else {
            return false
        }
        ViewCompat.setAlpha(item.itemView, 1f)
        ViewCompat.setTranslationX(item.itemView, 0f)
        ViewCompat.setTranslationY(item.itemView, 0f)
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun endAnimation(item: ViewHolder) {
        val view = item.itemView
        // this will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel()

        // TODO if some other animations are chained to end, how do we cancel them as well?
        pendingMoves.filter { it.holder === item }
                    .reversed()
                    .forEach {
                        ViewCompat.setTranslationY(view, 0f)
                        ViewCompat.setTranslationX(view, 0f)
                        dispatchMoveFinished(item)
                    }
        pendingMoves.removeAll { it.holder === item }
        endChangeAnimation(pendingChanges, item)

        if (pendingRemovals.remove(item)) {
            ViewHelper.clear(item.itemView)
            dispatchRemoveFinished(item)
        }
        if (pendingAdditions.remove(item)) {
            ViewHelper.clear(item.itemView)
            dispatchAddFinished(item)
        }

        changesList.reversed().forEach { changes ->
            endChangeAnimation(changes, item)
        }
        changesList.removeAll { changes -> changes.isEmpty() }

        movesList.reversed().forEach { moves ->
            moves.filter { it.holder === item }
                 .reversed()
                 .forEach {
                     ViewCompat.setTranslationY(view, 0f)
                     ViewCompat.setTranslationX(view, 0f)
                     dispatchMoveFinished(item)
                }
            moves.removeAll { it.holder === item }
        }
        movesList.removeAll { moves -> moves.isEmpty() }

        additionsList.filter { it.remove(item) }
                     .reversed()
                     .forEach {
                         ViewHelper.clear(item.itemView)
                         dispatchAddFinished(item)
                     }
        additionsList.removeAll { additions -> additions.isEmpty() }

        // animations should be ended by the cancel above.
        if (removeAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in removeAnimations list")
        }

        if (addAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in addAnimations list")
        }

        if (changeAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in changeAnimations list")
        }

        if (moveAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in moveAnimations list")
        }
        dispatchFinishedWhenDone()
    }

    override fun isRunning(): Boolean {
        return !pendingAdditions.isEmpty() ||
               !pendingChanges.isEmpty() ||
               !pendingMoves.isEmpty() ||
               !pendingRemovals.isEmpty() ||
               !moveAnimations.isEmpty() ||
               !removeAnimations.isEmpty() ||
               !addAnimations.isEmpty() ||
               !changeAnimations.isEmpty() ||
               !movesList.isEmpty() ||
               !additionsList.isEmpty() ||
               !changesList.isEmpty()
    }

    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call #dispatchAnimationsFinished() to notify any
     * listeners.
     */
    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    override fun endAnimations() {
        pendingMoves.reversed().forEach {
            val view = it.holder.itemView
            ViewCompat.setTranslationY(view, 0f)
            ViewCompat.setTranslationX(view, 0f)
            dispatchMoveFinished(it.holder)
        }
        pendingMoves.clear()

        pendingRemovals.reversed().forEach {
            dispatchRemoveFinished(it)
        }
        pendingRemovals.clear()

        pendingAdditions.reversed().forEach {
            ViewHelper.clear(it.itemView)
            dispatchAddFinished(it)
        }
        pendingAdditions.clear()

        pendingChanges.reversed().forEach {
            endChangeAnimationIfNecessary(it)
        }
        pendingChanges.clear()

        if (!isRunning) {
            return
        }

        movesList.reversed().forEach {
            val moves = it
            moves.reversed().forEach {
                val moveInfo = it
                val item = moveInfo.holder
                val view = item.itemView
                ViewCompat.setTranslationY(view, 0f)
                ViewCompat.setTranslationX(view, 0f)
                dispatchMoveFinished(moveInfo.holder)
            }
            moves.clear()
        }
        movesList.clear()

        additionsList.reversed().forEach {
            val additions = it
            additions.reversed().forEach {
                val item = it
                val view = item.itemView
                ViewCompat.setAlpha(view, 1f)
                dispatchAddFinished(item)
            }
            additions.clear()
        }
        additionsList.clear()

        changesList.reversed().forEach {
            val changes = it
            changes.reversed().forEach {
                endChangeAnimationIfNecessary(it)
            }
        }
        changesList.clear()

        cancelAll(removeAnimations)
        cancelAll(moveAnimations)
        cancelAll(addAnimations)
        cancelAll(changeAnimations)

        dispatchAnimationsFinished()
    }

    private fun cancelAll(viewHolders: List<ViewHolder>) {
        viewHolders.reversed().forEach {
            ViewCompat.animate(it.itemView).cancel()
        }
    }

    protected open class VpaListenerAdapter : ViewPropertyAnimatorListener {

        override fun onAnimationStart(view: View) {}

        override fun onAnimationEnd(view: View) {}

        override fun onAnimationCancel(view: View) {}
    }

    protected inner class DefaultAddVpaListener(internal var mViewHolder: RecyclerView.ViewHolder) : VpaListenerAdapter() {

        override fun onAnimationStart(view: View) {
            dispatchAddStarting(mViewHolder)
        }

        override fun onAnimationCancel(view: View) {
            ViewHelper.clear(view)
        }

        override fun onAnimationEnd(view: View) {
            ViewHelper.clear(view)
            dispatchAddFinished(mViewHolder)
            addAnimations.remove(mViewHolder)
            dispatchFinishedWhenDone()
        }
    }

    protected inner class DefaultRemoveVpaListener(internal var mViewHolder: RecyclerView.ViewHolder) : VpaListenerAdapter() {

        override fun onAnimationStart(view: View) {
            dispatchRemoveStarting(mViewHolder)
        }

        override fun onAnimationCancel(view: View) {
            ViewHelper.clear(view)
        }

        override fun onAnimationEnd(view: View) {
            ViewHelper.clear(view)
            dispatchRemoveFinished(mViewHolder)
            removeAnimations.remove(mViewHolder)
            dispatchFinishedWhenDone()
        }
    }
}
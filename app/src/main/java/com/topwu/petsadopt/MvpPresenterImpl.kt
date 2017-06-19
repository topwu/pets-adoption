package com.topwu.petsadopt

import java.lang.ref.SoftReference

abstract class MvpPresenterImpl<V : MvpView> : MvpPresenter<V> {

    private var viewReference: SoftReference<V>? = null

    override fun attachView(view: V) {
        viewReference = SoftReference(view)
        onViewAttached()
    }

    fun getView(): V? {
        return viewReference?.get()
    }

    override fun isViewAttached(): Boolean {
        return viewReference != null
    }

    override fun detachView() {
        viewReference?.clear()
        viewReference = null
    }

    fun onViewAttached() {
    }
}
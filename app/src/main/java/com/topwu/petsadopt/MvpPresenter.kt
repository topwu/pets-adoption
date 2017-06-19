package com.topwu.petsadopt

interface MvpPresenter<in V: MvpView> {
    fun attachView(view: V)
    fun detachView()
    fun isViewAttached(): Boolean
}
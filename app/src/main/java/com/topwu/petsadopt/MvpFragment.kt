package com.topwu.petsadopt

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View

abstract class MvpFragment<P : MvpPresenter<MvpView>> : Fragment(), MvpView {

    protected var presenter: P? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (presenter == null) {
            presenter = createPresenter()
        }
        presenter?.attachView(this)
    }

    protected abstract fun createPresenter(): P

    abstract fun onBackPressed()

    override fun onDestroy() {
        presenter?.detachView()
        super.onDestroy()
    }
}

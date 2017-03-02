package com.example.sveta.taxo;

/**
 * Created by Sveta on 02.03.2017.
 */

public class MainPresenterImpl implements MainPresenter {
    private MainViewImpl mainView;

    public MainPresenterImpl(MainViewImpl mainView) {
        this.mainView = mainView;
    }

    @Override
    public void order() {
        mainView.navigateToDetailOrder();
    }

    @Override
    public void plusFiveToOrder() {

    }
}

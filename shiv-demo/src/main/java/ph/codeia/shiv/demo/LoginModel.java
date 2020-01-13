package ph.codeia.shiv.demo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executor;

import javax.inject.Inject;

/*
 * This file is a part of the Shiv project.
 */


public class LoginModel extends ViewModel {
    private final MutableLiveData<Object> state = new MutableLiveData<>();
    private final Executor io;
    private String username = "";
    private String password = "";

    @Inject
    public LoginModel(Executor io) {
        this.io = io;
        state.setValue(new Object());
    }

    public LiveData<Object> state() {
        return state;
    }

    public void setUsername(CharSequence value) {
        username = value.toString();
    }

    public void setPassword(CharSequence value) {
        password = value.toString();
    }

    public void login() {

    }
}

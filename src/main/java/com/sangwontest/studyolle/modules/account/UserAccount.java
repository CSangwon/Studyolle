package com.sangwontest.studyolle.modules.account;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

//spring security 가 다루는 유저정보와 우리 도메인에서 다루는 유저정보의 사이의 갭을 매꿔주는 어댑터 역할
//User는 spring security 에서 오는 것이다.
@Getter
public class UserAccount extends User {

    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
        //spring security 가 다루는 유저정보를 우리가 갖고있는 유저정보와 연동해주는 것
    }
}

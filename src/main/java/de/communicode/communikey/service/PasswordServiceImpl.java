/*
 * Copyright (C) communicode AG - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * 2016
*/
package de.communicode.communikey.service;

import de.communicode.communikey.domain.Password;
import de.communicode.communikey.repository.PasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PasswordServiceImpl implements PasswordService {

    @Autowired
    private PasswordRepository passwordRepository;

    @Override
    public Set<Password> getAllPasswords() {
        return StreamSupport.stream(passwordRepository.findAll().spliterator(), false)
            .collect(Collectors.toSet());
    }

    @Override
    public Password getPasswordById(long id) {
        return passwordRepository.findOne(id);
    }

    @Override
    public Password getPasswordByCreationDate(Timestamp timestamp) {
        return passwordRepository.findOneByCreationTimestamp(timestamp);
    }

    @Override
    public void deletePassword(Password password) {
        passwordRepository.delete(password);
    }

    @Override
    public void modifyPasswordValue(Password password, String newValue) {
        password.setValue(newValue);
        passwordRepository.save(password);
    }

    @Override
    public Password savePassword(Password password) {
        return passwordRepository.save(password);
    }
}

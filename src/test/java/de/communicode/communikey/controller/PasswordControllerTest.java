/*
 * Copyright (C) communicode AG - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * 2016
*/
package de.communicode.communikey.controller;

import static de.communicode.communikey.CommunikeyConstants.ENDPOINT_PASSWORDS;
import static de.communicode.communikey.CommunikeyConstants.ENDPOINT_ROOT;
import static de.communicode.communikey.CommunikeyConstants.REQUEST_PASSWORD_NEW;
import static de.communicode.communikey.CommunikeyConstants.TEMPLATE_PASSWORD_EDIT;
import static de.communicode.communikey.CommunikeyConstants.TEMPLATE_PASSWORD_NEW;
import static de.communicode.communikey.CommunikeyConstants.asRedirect;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import de.communicode.communikey.CommunikeyApplication;
import de.communicode.communikey.domain.Password;
import de.communicode.communikey.repository.PasswordRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

/**
 * Unit tests for the {@link PasswordController} class.
 *
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CommunikeyApplication.class)
@WebAppConfiguration
public class PasswordControllerTest {
    private MockMvc mockMvc;
    private PasswordController passwordController;
    @Autowired
    private PasswordRepository passwordRepository;
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private Filter springSecurityFilterChain;

    @Before
    public void setup() {
        passwordController = new PasswordController();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .addFilters(springSecurityFilterChain)
            .defaultRequest(get(ENDPOINT_ROOT).with(user("user").password("pass").roles("USER")))
            .build();
    }

    @Test
    public void passwordsEndpoint() throws Exception {
        mockMvc.perform(get(ENDPOINT_PASSWORDS))
            .andExpect(status().isOk())
            .andExpect(view().name(ENDPOINT_PASSWORDS));
    }

    @Test
    public void passwordsNewEndpoints() throws Exception {
        mockMvc.perform(get(REQUEST_PASSWORD_NEW)
            .with(csrf())
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("password"))
            .andExpect(model().attributeExists("newPasswordForm"))
            .andExpect(model().size(2))
            .andExpect(view().name(TEMPLATE_PASSWORD_NEW));

        mockMvc.perform(post(REQUEST_PASSWORD_NEW)
            .with(csrf())
        )
            .andExpect(view().name(asRedirect(ENDPOINT_PASSWORDS)))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    public void passwordsEditEndpoints() throws Exception {
        Password password = new Password("yogurt");
        passwordRepository.save(password);

        mockMvc.perform(get("/passwords/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name(TEMPLATE_PASSWORD_EDIT))
            .andExpect(model().attributeExists("password"))
            .andExpect(model().attributeExists("editPasswordForm"))
            .andExpect(model().size(2));

        mockMvc.perform(post("/passwords/1/edit")
            .with(csrf())
        )
            .andExpect(view().name(asRedirect(ENDPOINT_PASSWORDS)))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    public void passwordsDeleteRedirectionEndpoint() throws Exception {
        Password password = new Password("yogurt");
        passwordRepository.save(password);

        mockMvc.perform(get("/passwords/1/delete"))
            .andExpect(view().name(asRedirect(ENDPOINT_PASSWORDS)))
            .andExpect(status().is3xxRedirection());
    }
}

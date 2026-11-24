package org.talentlink.talentlink.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.security.RoleMappingHelper;
import org.talentlink.talentlink.service.AdminUserService;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    private AdminUserController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminUserController(adminUserService, new RoleMappingHelper());
    }

    @Test
    void listUsers_populatesModelAndReturnsAdminUsersView() {
        when(adminUserService.getUsers("test", 3L, "active", 0, 10))
                .thenReturn(new PageImpl<>(List.of(new User()), PageRequest.of(0, 10), 1));

        Model model = new ConcurrentModel();
        Principal principal = () -> "admin@talentlink.org";

        String viewName = controller.listUsers("test", 3L, "active", 0, 10, principal, model);

        assertEquals("admin/users", viewName);
        assertEquals("test", model.getAttribute("keyword"));
        assertEquals("admin@talentlink.org", model.getAttribute("currentAdminEmail"));
        assertNotNull(model.getAttribute("usersPage"));
    }

    @Test
    void createPartner_redirectsWithSuccessFlash() {
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();

        String view = controller.createPartner(
                "Partner A",
                "partner@company.com",
                "123456",
                "0912345678",
                "Company XYZ",
                redirectAttrs
        );

        assertEquals("redirect:/admin/users", view);
        assertNotNull(redirectAttrs.getFlashAttributes().get("successMessage"));
        verify(adminUserService).createPartnerAccount(
                "Partner A", "partner@company.com", "123456", "0912345678", "Company XYZ"
        );
    }

    @Test
    void toggleStatus_selfLockRejected_redirectsWithErrorFlashAndParams() {
        when(adminUserService.toggleUserActiveStatus(1L, "admin@talentlink.org"))
                .thenThrow(new IllegalStateException("Bạn không thể tự khoá tài khoản của chính mình."));

        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();
        Principal principal = () -> "admin@talentlink.org";

        String view = controller.toggleStatus(1L, "admin", null, null, 0, 10, principal, redirectAttrs);

        assertEquals("redirect:/admin/users", view);
        assertNotNull(redirectAttrs.getFlashAttributes().get("errorMessage"));
        assertEquals("admin", redirectAttrs.asMap().get("keyword"));
        assertEquals("0", redirectAttrs.asMap().get("page"));
        assertEquals("10", redirectAttrs.asMap().get("size"));
    }
}

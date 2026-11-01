package br.com.nuvola.portability.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nuvola.portability.config.JwtConfig;
import br.com.nuvola.portability.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import br.com.nuvola.portability.config.SecretsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = { AuthController.class, AuthSecurityTest.ProtectedController.class })
@Import({ SecurityConfig.class, JwtConfig.class, AuthService.class, TokenService.class, UserDirectory.class,
        CurrentUser.class, AuthSecurityTest.ProtectedController.class })
@EnableConfigurationProperties(SecretsProperties.class)
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserDirectory users;

    @Test
    void returnsTokenForValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "usuario": "operador", "senha": "lola123" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.perfil").value("OPERADOR"))
                .andExpect(jsonPath("$.nome").value("Operador Nuvola"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void acceptsUsernameInAnyCase() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "usuario": "ben001", "senha": "lola123" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("BENEFICIARIO"))
                .andExpect(jsonPath("$.nome").value("Lorraine Cristina"));
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "usuario": "operador", "senha": "errada" }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void rejectsUnknownUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "usuario": "ninguem", "senha": "lola123" }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/beneficiarios/BEN001/agenda"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("SESSAO_EXPIRADA"));
    }

    @Test
    void rejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/api/beneficiarios/BEN001/agenda")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("SESSAO_EXPIRADA"));
    }

    @Test
    void allowsOperatorOnOperatorRoute() throws Exception {
        mockMvc.perform(get("/api/beneficiarios/BEN001/agenda")
                .header(HttpHeaders.AUTHORIZATION, bearerFor("operador")))
                .andExpect(status().isOk());
    }

    @Test
    void forbidsBeneficiaryOnOperatorRoute() throws Exception {
        mockMvc.perform(get("/api/beneficiarios/BEN001/agenda")
                .header(HttpHeaders.AUTHORIZATION, bearerFor("BEN001")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    void currentUserReadsUsernameAndRoleFromToken() throws Exception {
        mockMvc.perform(get("/api/autorizacao")
                .header(HttpHeaders.AUTHORIZATION, bearerFor("ben001")))
                .andExpect(status().isOk())
                .andExpect(content().string("BEN001 BENEFICIARIO"));
    }

    private String bearerFor(String username) {
        return "Bearer " + tokenService.issue(users.findByUsername(username).orElseThrow());
    }

    @RestController
    @RequestMapping("/api")
    public static class ProtectedController {

        private final CurrentUser currentUser;

        public ProtectedController(CurrentUser currentUser) {
            this.currentUser = currentUser;
        }

        @GetMapping("/beneficiarios/{id}/agenda")
        public String schedule(@PathVariable String id) {
            return "agenda de " + id;
        }

        @GetMapping("/autorizacao")
        public String whoAmI() {
            return currentUser.username() + " " + currentUser.role();
        }

    }

}

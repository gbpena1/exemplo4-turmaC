package br.senac.sp.tads.dsw.exemplo5.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.senac.sp.tads.dsw.exemplo5.dto.DepartamentoRequestDTO;
import br.senac.sp.tads.dsw.exemplo5.model.Departamento;
import br.senac.sp.tads.dsw.exemplo5.repository.DepartamentoRepository;
// Para injeção de dependência via @Autowired:
import tools.jackson.databind.ObjectMapper;
// Para injeção de dependência direta:
// import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
public class DepartamentoControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartamentoRepository repository;

    // Para injeção de dependência via @Autowired:
    @Autowired
    private ObjectMapper objectMapper;
    
    // Para injeção de dependência direta:
    // private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deveCriarDepartamentoComSucesso() throws Exception {
        // 1 - Criar o objeto que queremos enviar
        DepartamentoRequestDTO dto = new DepartamentoRequestDTO("Tecnologia da Informação", 150000.00);
        String jsonRequisicao = objectMapper.writeValueAsString(dto);

        // 2 - Enviar o POST para a url da API
        mockMvc.perform(post("/api/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
        // 3 - Verificar se a resposta é o esperado        
                .andExpect(status().isCreated()) // HTTP 201
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Tecnologia da Informação"));
    }

    @Test
    void deveListarTodosOsDepartamentos() throws Exception {
        DepartamentoRequestDTO dto = new DepartamentoRequestDTO("Tecnologia da Informação", 150000.00);
        
        Departamento departamento = new Departamento();
        departamento.setNome(dto.nome());
        departamento.setOrcamento(dto.orcamento());
        
        repository.save(departamento);

        mockMvc.perform(get("/api/departamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Tecnologia da Informação")); // Posição zero do array
    }

    @Test
    void deveBuscarDepartamentoPorId() throws Exception {
        DepartamentoRequestDTO dto = new DepartamentoRequestDTO("Tecnologia da Informação", 150000.00);
        
        Departamento departamento = new Departamento();
        departamento.setNome(dto.nome());
        departamento.setOrcamento(dto.orcamento());

        Departamento departamentoSalvo = repository.save(departamento);

        mockMvc.perform(get("/api/departamentos/" + departamentoSalvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Tecnologia da Informação"));
    }

    @Test
    void deveAtualizarDepartamento() throws Exception {
        // 1 - Prepara o registro antigo no banco de dados
        DepartamentoRequestDTO dto = new DepartamentoRequestDTO("Tecnologia da Informação", 150000.00);
        
        Departamento departamento = new Departamento();
        departamento.setNome(dto.nome());
        departamento.setOrcamento(dto.orcamento());
  
        repository.save(departamento); // coloca 1 registro no banco de dados

        // 2 - Cria os novos dados para o PUT
        departamento.setOrcamento(50000.00);
        String jsonAtualizado = objectMapper.writeValueAsString(departamento);

        // 3 - Executa e verifica se atualizou
        mockMvc.perform(put("/api/departamentos/" + departamento.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAtualizado))       
                .andExpect(status().isOk()) // HTTP 200
                .andExpect(jsonPath("$.orcamento").value(50000.00));
    }

    @Test
    void deveApagarDepartamento() throws Exception {
        DepartamentoRequestDTO dto = new DepartamentoRequestDTO("Tecnologia da Informação", 150000.00);
        
        Departamento departamento = new Departamento();
        departamento.setNome(dto.nome());
        departamento.setOrcamento(dto.orcamento());

        departamento = repository.save(departamento);

        // Deleta e espera 204 No Content
        mockMvc.perform(delete("/api/departamentos/" + departamento.getId()))
                .andExpect(status().isNoContent());

        // Confirma que apagou tentando buscar (espera 404 Not Found)
        mockMvc.perform(get("/api/departamentos/" + departamento.getId()))
                .andExpect(status().isNotFound());
    }
}

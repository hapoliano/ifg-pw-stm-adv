package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.atty.stm.model.Files;
import org.atty.stm.model.Processo;
import org.atty.stm.model.Usuario;
import org.atty.stm.repository.FilesRepository;
import org.atty.stm.repository.ProcessoRepository;
import org.atty.stm.util.MapperUtils;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class FilesService {

    @Inject
    FilesRepository filesRepository;

    @Inject
    ProcessoRepository processoRepository;

    @Inject
    AuditoriaService auditoriaService;

    @Transactional
    public org.atty.stm.model.dto.FilesDTO uploadFile(org.atty.stm.model.dto.FilesDTO dto, Usuario uploader, String ip, String ua) {
        Processo p = processoRepository.findById(dto.getProcessoId());
        if (p == null) throw new RuntimeException("Processo não encontrado");

        Files f = new Files();
        f.nomeArquivo = dto.getNome();
        f.caminhoArquivo = dto.getUrl();
        f.tipoArquivo = dto.getTipo();
        f.tamanho = dto.getTamanho() != null ? dto.getTamanho() : 0L;
        f.processo = p;
        f.usuario = uploader;
        f.dataUpload = LocalDateTime.now();

        filesRepository.persist(f);
        auditoriaService.registrarAcaoSimples(uploader, "UPLOAD", "FILE", f.id, "Upload de arquivo: " + f.nomeArquivo, ip, ua);
        return MapperUtils.toDTO(f);
    }

    @Transactional
    public boolean deletarFile(Long id, Usuario user, String ip, String ua) {
        Files f = filesRepository.findById(id);
        if (f == null) return false;
        boolean ok = filesRepository.deleteById(id);
        if (ok) auditoriaService.registrarAcaoSimples(user, "DELECAO", "FILE", id, "Arquivo deletado", ip, ua);
        return ok;
    }

    public List<org.atty.stm.model.dto.FilesDTO> listarPorProcesso(Long processoId) {
        return MapperUtils.toDTOList(filesRepository.findByProcessoId(processoId), org.atty.stm.model.dto.FilesDTO.class);
    }
}

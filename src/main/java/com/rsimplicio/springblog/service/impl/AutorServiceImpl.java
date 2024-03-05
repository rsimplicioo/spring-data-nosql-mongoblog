package com.rsimplicio.springblog.service.impl;

import com.rsimplicio.springblog.model.Autor;
import com.rsimplicio.springblog.repository.AutorRepository;
import com.rsimplicio.springblog.service.AutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class AutorServiceImpl implements AutorService {
    @Autowired
    private AutorRepository autorRepository;
    @Override
    public Autor criar(Autor autor) {
        return autorRepository.save(autor);
    }
    @Override
    public Autor obterCodigo(String codigo) {
        return autorRepository.findById(codigo).orElseThrow(() -> new IllegalArgumentException("Autor não existe!"));
    }
}

package com.rsimplicio.springblog.service;

import com.rsimplicio.springblog.model.Autor;

public interface AutorService {
    public Autor criar(Autor autor);
    public Autor obterCodigo(String codigo);
}

package com.rsimplicio.springblog.controller;

import com.rsimplicio.springblog.model.Artigo;
import com.rsimplicio.springblog.model.Autor;
import com.rsimplicio.springblog.service.AutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(value = "/autores")
public class AutorController {
    @Autowired
    private AutorService autorService;
    @PostMapping
    public Autor criar(@RequestBody Autor autor) {
        return this.autorService.criar(autor);
    }
    @GetMapping("/{codigo}")
    public Autor obterPorCodigo(@PathVariable String codigo) {
        return this.autorService.obterCodigo(codigo);
    }
}

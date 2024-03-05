package com.rsimplicio.springblog.controller;

import com.rsimplicio.springblog.model.Artigo;
import com.rsimplicio.springblog.service.ArtigoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
@RestController
@RequestMapping(value = "/artigos")
public class ArtigoController {
    @Autowired
    private ArtigoService artigoService;
    @GetMapping
    public List<Artigo> obterTodos(){
        return this.artigoService.obterTodos();
    }
    @GetMapping("/{codigo}")
    public Artigo obterPorCodigo(@PathVariable String codigo) {
        return this.artigoService.obterPorCodigo(codigo);
    }
    @PostMapping
    public Artigo criar(@RequestBody Artigo artigo) {
        return this.artigoService.criar(artigo);
    }
    @GetMapping("/maiordata")
    public List<Artigo> findByDataGreaterThan(@RequestParam("data") LocalDateTime data) {
        return this.artigoService.findByDataGreaterThan(data);
    }
    @GetMapping("/data-status")
    public List<Artigo> findByDataAndStatus(@RequestParam("data")LocalDateTime data, @RequestParam("status")Integer status) {
        return this.artigoService.findByDataAndStatus(data, status);
    }
    @PutMapping
    public void atualizar(@RequestBody Artigo artigo) {
        this.artigoService.atualizar(artigo);
    }
    @PutMapping("/{id}")
    public void atualizarArtigo(@PathVariable String id, @RequestBody String novaURL) {
        this.artigoService.atualizarArtigo(id, novaURL);
    }
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        this.artigoService.deleteById(id);
    }
    @DeleteMapping("/delete")
    public void deleteArtigoById(@RequestParam("Id") String id) {
        this.artigoService.deleteArtigoById(id);
    }
}

package com.rsimplicio.springblog.service.impl;

import com.rsimplicio.springblog.model.Artigo;
import com.rsimplicio.springblog.model.Autor;
import com.rsimplicio.springblog.repository.ArtigoRepository;
import com.rsimplicio.springblog.repository.AutorRepository;
import com.rsimplicio.springblog.service.ArtigoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class ArtigoServiceImpl implements ArtigoService {
    private final MongoTemplate mongoTemplate;

    public ArtigoServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    private ArtigoRepository artigoRepository;
    @Autowired
    private AutorRepository autorRepository;
    @Override
    public List<Artigo> obterTodos() {
        return this.artigoRepository.findAll();
    }
    @Override
    public Artigo obterPorCodigo(String codigo) {
        return this.artigoRepository.findById(codigo).orElseThrow(() -> new IllegalArgumentException("Artigo não existe!"));
    }
    @Override
    public Artigo criar(Artigo artigo) {
        if(artigo.getAutor().getCodigo() != null) {
            Autor autor = this.autorRepository
                    .findById(artigo.getAutor().getCodigo())
                    .orElseThrow(() -> new IllegalArgumentException("Autor inexistente!"));
            artigo.setAutor(autor);
        } else {
            artigo.setAutor(null);
        }
        return this.artigoRepository.save(artigo);
    }
    @Override
    public List<Artigo> findByDataGreaterThan(LocalDateTime data) {
        Query query = new Query(Criteria.where("data").gt(data));
        return this.mongoTemplate.find(query, Artigo.class);
    }
    @Override
    public List<Artigo> findByDataAndStatus(LocalDateTime data, Integer status) {
        Query query = new Query(Criteria.where("data").is(data).and("status").is(status));
        return this.mongoTemplate.find(query, Artigo.class);
    }
    @Override
    public void atualizar(Artigo updateArtigo) {
        this.artigoRepository.save(updateArtigo);
    }
    @Override
    public void atualizarArtigo(String id, String novaURL) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("url", novaURL);
        this.mongoTemplate.updateFirst(query, update, Artigo.class);
    }
    @Override
    public void deleteById(String id) {
        this.artigoRepository.deleteById(id);
    }
    @Override
    public void deleteArtigoById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        this.mongoTemplate.remove(query, Artigo.class);
    }
}

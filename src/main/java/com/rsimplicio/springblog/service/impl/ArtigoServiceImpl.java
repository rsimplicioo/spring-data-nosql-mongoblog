package com.rsimplicio.springblog.service.impl;

import com.rsimplicio.springblog.model.Artigo;
import com.rsimplicio.springblog.model.ArtigoStatusCount;
import com.rsimplicio.springblog.model.Autor;
import com.rsimplicio.springblog.model.AutorTotalArtigo;
import com.rsimplicio.springblog.repository.ArtigoRepository;
import com.rsimplicio.springblog.repository.AutorRepository;
import com.rsimplicio.springblog.service.ArtigoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class ArtigoServiceImpl implements ArtigoService {
    private final MongoTemplate mongoTemplate;
    public ArtigoServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    private MongoTransactionManager transactionManager;

    @Autowired
    private ArtigoRepository artigoRepository;
    @Autowired
    private AutorRepository autorRepository;
    @Override
    public List<Artigo> obterTodos() {
        return this.artigoRepository.findAll();
    }
    @Transactional(readOnly = true)
    @Override
    public Artigo obterPorCodigo(String codigo) {
        return this.artigoRepository.findById(codigo).orElseThrow(() -> new IllegalArgumentException("Artigo não existe!"));
    }

    @Override
    public ResponseEntity<?> criarArtigoComAutor(Artigo artigo, Autor autor) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            try {
                autorRepository.save(autor);
                artigo.setData(LocalDateTime.now());
                artigo.setAutor(autor);
                artigoRepository.save(artigo);
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Erro ao criar artigo com autor: " + e.getMessage());
            }
            return null;
        });
        return null;
    }

    @Override
    public void excluirArtigoEAutor(Artigo artigo) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            try {
                artigoRepository.delete(artigo);
                Autor autor = artigo.getAutor();
                autorRepository.delete(autor);
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Erro ao excluir artigo e autor: " + e.getMessage());
            }
            return null;
        });
    }

    //    @Transactional
//    @Override
//    public Artigo criar(Artigo artigo) {
//        if(artigo.getAutor().getCodigo() != null) {
//            Autor autor = this.autorRepository
//                    .findById(artigo.getAutor().getCodigo())
//                    .orElseThrow(() -> new IllegalArgumentException("Autor inexistente!"));
//            artigo.setAutor(autor);
//        } else {
//            artigo.setAutor(null);
//        }
//
//        try {
//            return this.artigoRepository.save(artigo);
//        } catch(OptimisticLockingFailureException ex) {
//            Artigo artigoAtualizado = artigoRepository.findById(artigo.getCodigo()).orElse(null);
//            if(artigoAtualizado != null) {
//                artigoAtualizado.setTitulo(artigo.getTitulo());
//                artigoAtualizado.setData(artigo.getData());
//                artigoAtualizado.setTexto(artigo.getTexto());
//                artigoAtualizado.setUrl(artigo.getUrl());
//                artigoAtualizado.setStatus(artigo.getStatus());
//                artigoAtualizado.setAutor(artigo.getAutor());
//
//                artigoAtualizado.setVersion(artigoAtualizado.getVersion() + 1);
//                return this.artigoRepository.save(artigoAtualizado);
//            } else {
//                throw new RuntimeException("Artigo não encontrado: " + artigo.getCodigo());
//            }
//        }
//    }
//    @Override
//    public ResponseEntity<?> criar(Artigo artigo) {
//        if(artigo.getAutor().getCodigo() != null) {
//            Autor autor = this.autorRepository
//                    .findById(artigo.getAutor().getCodigo())
//                    .orElseThrow(() -> new IllegalArgumentException("Autor inexistente!"));
//            artigo.setAutor(autor);
//        } else {
//            artigo.setAutor(null);
//        }
//
//        try {
//            this.artigoRepository.save(artigo);
//            return ResponseEntity.status(HttpStatus.CREATED).build();
//        } catch (DuplicateKeyException e) {
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body("Artigo já existe na coleção!");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Erro ao criar artigo: " + e.getMessage());
//        }
//    }

    @Override
    public ResponseEntity<?> atualizarArtigo(String id, Artigo artigo) {
        try {
            Artigo artigoExistente = this.artigoRepository.findById(id).orElse(null);
            if(artigoExistente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artigo não encontrado na coleção!");
            }

            artigoExistente.setCodigo(artigo.getCodigo());
            artigoExistente.setTitulo(artigo.getTitulo());
            artigoExistente.setData(artigo.getData());
            artigoExistente.setTexto(artigo.getTexto());
            artigoExistente.setUrl(artigo.getUrl());
            artigoExistente.setStatus(artigo.getStatus());
            artigoExistente.setAutor(artigo.getAutor());
            this.artigoRepository.save(artigoExistente);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar artigo: " + e.getMessage());
        }
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
    @Transactional
    @Override
    public void atualizar(Artigo updateArtigo) {
        this.artigoRepository.save(updateArtigo);
    }

    @Transactional
    @Override
    public void atualizarArtigo(String id, String novaURL) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("url", novaURL);
        this.mongoTemplate.updateFirst(query, update, Artigo.class);
    }

    @Transactional
    @Override
    public void deleteById(String id) {
        this.artigoRepository.deleteById(id);
    }
    @Transactional
    @Override
    public void deleteArtigoById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        this.mongoTemplate.remove(query, Artigo.class);
    }
    @Override
    public List<Artigo> findByStatusAndDataGreaterThan(Integer status, LocalDateTime data) {
        return this.artigoRepository.findByStatusAndDataGreaterThan(status, data);
    }
    @Override
    public List<Artigo> obterArtigoPorDataHora(LocalDateTime de, LocalDateTime ate) {
        return this.artigoRepository.obterArtigoPorDataHora(de, ate);
    }
    @Override
    public List<Artigo> encontrarArtigosComplexos(Integer status, LocalDateTime data, String titulo) {
        Criteria criteria = new Criteria();
        if(data != null) {
            criteria.and("data").lte(data);
        }

        if(status != null) {
            criteria.and("status").is(status);
        }

        if(titulo != null && !titulo.isEmpty()) {
            criteria.and("titulo").regex(titulo, "i");
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Artigo.class);
    }
    @Override
    public Page<Artigo> listaArtigos(Pageable pageable) {
        Sort sort = Sort.by("titulo").ascending();
        Pageable paginacao = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return this.artigoRepository.findAll(paginacao);
    }
    @Override
    public List<Artigo> findByStatusOrderByTituloAsc(Integer status) {
        return this.artigoRepository.findByStatusOrderByTituloAsc(status);
    }
    @Override
    public List<Artigo> obterArtigoPorStatusComOrdenacao(Integer status) {
        return this.artigoRepository.obterArtigoPorStatusComOrdenacao(status);
    }
    @Override
    public List<Artigo> findByTexto(String searchTerm) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingPhrase(searchTerm);
        Query query = TextQuery.queryText(criteria).sortByScore();
        return mongoTemplate.find(query, Artigo.class);
    }
    @Override
    public List<ArtigoStatusCount> contarArtigosPorStatus() {
        TypedAggregation<Artigo> aggregation =
                Aggregation.newAggregation(
                        Artigo.class,
                        Aggregation.group("status").count().as("quantidade"),
                        Aggregation.project("quantidade").and("status").previousOperation()
                );
        AggregationResults<ArtigoStatusCount> results = mongoTemplate.aggregate(aggregation, ArtigoStatusCount.class);
        return results.getMappedResults();
    }
    @Override
    public List<AutorTotalArtigo> calcularTotalArtigosPorAutorNoPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        TypedAggregation<Artigo> aggregation =
                Aggregation.newAggregation(
                        Artigo.class,
                        Aggregation.match(
                                Criteria.where("data").gte(dataInicio.atStartOfDay()).
                                        lt(dataFim.plusDays(1).atStartOfDay())
                        ),
                        Aggregation.group("autor").count().as("totalArtigos"),
                        Aggregation.project("totalArtigos").and("autor").previousOperation()
                );
        AggregationResults<AutorTotalArtigo> results = mongoTemplate.aggregate(aggregation, AutorTotalArtigo.class);
        return results.getMappedResults();
    }
}

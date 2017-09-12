package com.algaworks.algamoney.api.repository.pessoa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.algaworks.algamoney.api.model.Pessoa;
import com.algaworks.algamoney.api.model.Pessoa_;
import com.algaworks.algamoney.api.repository.filter.PessoaFilter;

public class PessoaRepositoryImpl implements PessoaRepositoryQuery {

	@PersistenceContext
	private EntityManager manager;
	
	@Override
	public Page<Pessoa> filtrar(PessoaFilter filter, Pageable pageable) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();

		CriteriaQuery<Pessoa> criteria = builder.createQuery(Pessoa.class);

		Root<Pessoa> root = criteria.from(Pessoa.class);

		Predicate[] predicate = criarRestricoes(filter, builder, root);
		criteria.where(predicate);

		TypedQuery<Pessoa> query = manager.createQuery(criteria);
		
		adicionarRestricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(filter));
		
	}
	
	private Predicate[] criarRestricoes(PessoaFilter filter, CriteriaBuilder builder, Root<Pessoa> root) {

		List<Predicate> predicates = new ArrayList<>();

		if (!StringUtils.isEmpty(filter.getNome())) {
			predicates.add(builder.like(builder.lower(root.get(Pessoa_.nome)),
					"%" + filter.getNome().toLowerCase() + "%"));
		}
		
		return predicates.toArray(new Predicate[predicates.size()]);

	}
	
	private void adicionarRestricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {
		int pageAtual = pageable.getPageNumber();
		int totalRegistroPorPagina = pageable.getPageSize();
		int primeiroRegistroPagina = pageAtual * totalRegistroPorPagina;
		query.setFirstResult(primeiroRegistroPagina);
		query.setMaxResults(totalRegistroPorPagina);
	}
	
	private Long total(PessoaFilter filter) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Pessoa> root = criteria.from(Pessoa.class);
		Predicate[] predicates = criarRestricoes(filter, builder, root);
		criteria.where(predicates);
		criteria.select(builder.count(root));
		return manager.createQuery(criteria).getSingleResult();
	}

}

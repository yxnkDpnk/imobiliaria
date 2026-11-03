	package com.example.primeiro_projeto.services;
	
	import java.util.List;
	import java.util.Optional;
	
	import org.springframework.stereotype.Service;
	import org.springframework.transaction.annotation.Transactional;
	
	import com.example.primeiro_projeto.models.ImovelModel;
	import com.example.primeiro_projeto.repositories.ImovelRepository;
	
	@Service
	public class ImovelService {
	    private final ImovelRepository repository;
	
	    public ImovelService(ImovelRepository repository) {
	        this.repository = repository;
	    }
	
	    public List<ImovelModel> getAll() {
	        List<ImovelModel> list = repository.findAll();
	        return list;
	    }
	
	    public ImovelModel find(Integer id) {
	        Optional<ImovelModel> model = repository.findById(id);
	        return model.orElse(null);
	    }
	
	    public ImovelModel insert(ImovelModel model) {
	        return repository.save(model);
	    }
	 
	    public ImovelModel update(ImovelModel model) {
	        try {
	            if(find(model.getId())!=null){
	                return repository.save(model);
	            }
	            return null;
	        } catch (Exception e) {
	            return null;
	        }
	
	    }

	    @Transactional
	    public ImovelModel updateStatus(Integer id, String status) {
	        ImovelModel imovel = find(id);
	        if (imovel == null) {
	            return null;
	        }

	        // AULA-JWT: Carregamos o imóvel existente e mudamos somente o campo status.
	        imovel.setStatus(status);
	        return repository.save(imovel);
	    }
	
	    public void delete(Integer id) {
	        repository.deleteById(id);
	    }
	}
	
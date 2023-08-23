package br.com.jffw.cae.services;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jffw.cae.dto.VagaVeiculosDTO;
import br.com.jffw.cae.dto.VeiculoDTO;
import br.com.jffw.cae.models.Proprietario;
import br.com.jffw.cae.models.Vaga;
import br.com.jffw.cae.models.Veiculo;
import br.com.jffw.cae.repository.VagaRepository;
import br.com.jffw.cae.repository.VeiculoRepository;

@Service
public class VeiculoService {
	@Autowired
	private VeiculoRepository veiculoRepository;

	@Autowired
	private VagaRepository vagaRepository;

//    public List<VagaVeiculoDTO > listarVeiculos(String id) {
//        return veiculoRepository.getVagaVeiculoDTOById(id);
//    }
//    

	public List<Veiculo> listarVeiculos() {
		return veiculoRepository.findAll();
	}

	public VeiculoDTO incluirVeiculo(Map<String, String> dados) throws ParseException {

		String placa = dados.get("placa");
		String cor = dados.get("cor");
		String modelo = dados.get("modelo");
		int idVaga = Integer.parseInt(dados.get("idVaga"));
		
	    Vaga existingVaga = vagaRepository.findById(idVaga).orElse(null);
	    if (existingVaga == null) {
	        throw new IllegalArgumentException("Vaga não localizada.");
	    }
	
		if ((placa.isBlank()) || (cor.isBlank()) || (modelo.isBlank())) {
			throw new NullPointerException("A placa, cor e modelo devem ser informados.");
		}
		
		Veiculo vc = veiculoRepository.findByPlacaAndCorAndModelo(placa, cor, modelo);

		if (!Optional.ofNullable(vc).isEmpty()) {
			throw new RuntimeException("Este veiculo " + placa + " já existe");
		}

		List<Veiculo> veiculosdaVaga = existingVaga.getVeiculos();
		if (!veiculosdaVaga.isEmpty()) {
			throw new IllegalArgumentException("A vaga já está vinculada a outro veículo.");
		}

		Optional<Vaga> vagaOptional = vagaRepository.findById(idVaga);
		if (!vagaOptional.isPresent()) {
			throw new IllegalArgumentException("A vaga com o ID fornecido não existe.");
		}

		// criando o objeto veiculo
		Veiculo veiculo = new Veiculo();
		veiculo.setVaga(existingVaga);
		veiculo.setPlaca(placa);
		veiculo.setCor(cor);
		veiculo.setModelo(modelo);

		veiculoRepository.save(veiculo);

		return new VeiculoDTO(veiculo.getVaga().getId(), veiculo.getPlaca(), veiculo.getCor(), veiculo.getModelo());

	}

	public String alterarVeiculo(Map<String, String> dados, String placa) {
        Veiculo veiculo = veiculoRepository.findById(placa)
                .orElseThrow(() -> new EntityNotFoundException("O veículo a ser alterado não existe."));

		
		String cor = dados.get("cor");
		String modelo = dados.get("modelo");
		int idVaga = Integer.parseInt(dados.get("idVaga"));

//		Optional<Veiculo> veiculoOptional = veiculoRepository.findById(placa);
//		if (!veiculoOptional.isPresent()) {
//			throw new IllegalArgumentException("O veículo com a placa fornecida não existe.");
//		}

		Optional<Vaga> vagaOptional = vagaRepository.findById(idVaga);
		if (!vagaOptional.isPresent()) {
			throw new IllegalArgumentException("A vaga com o ID fornecido não existe.");
		}

		if (((cor.isBlank()) || (modelo.isBlank()))) {
			throw new NullPointerException("A cor e modelo devem ser informados.");
		}

		
        List<Veiculo> veiculosVinculados = veiculoRepository
                .findByVagaAndNotPlaca(idVaga, placa);
        if (!veiculosVinculados.isEmpty()) {
            throw new IllegalArgumentException("A vaga já está vinculada a outro veículo.");
        }
		
		veiculo.setCor(cor);
		veiculo.setModelo(modelo);
		veiculo.setVaga(vagaOptional.get());

		veiculoRepository.save(veiculo);

		return "Alteração realizada com sucesso!";
	}

	public String remover(String placa) {
		try {
			veiculoRepository.deleteById(placa);
			return String.format("Veículo %s removido com sucesso", placa);
		} catch (Exception e) {
			throw new RuntimeException("Veiculo informado não existe.");
		}
	}

	public List<VagaVeiculosDTO> listarVeiculos1() {
		return veiculoRepository.getVagaVeiculoDTO();

	}

	public VagaVeiculosDTO listarVeiculosPorPlaca(String placa) {
		Veiculo veiculo = veiculoRepository.findById(placa)
				.orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado"));
		VagaVeiculosDTO dto = new VagaVeiculosDTO();
		dto.setPlaca(veiculo.getPlaca());
		dto.setCor(veiculo.getCor());
		dto.setModelo(veiculo.getModelo());

		Vaga vaga = veiculo.getVaga();
		if (vaga != null) {
			dto.setVaga(vaga.getId());
			dto.setNumero(vaga.getNumero());
			dto.setBloco(vaga.getBloco());
		}

		return dto;
	}

	public List<VagaVeiculosDTO> listarVeiculosDTO() {
		List<Veiculo> veiculos = veiculoRepository.findAll();
		List<VagaVeiculosDTO> dtos = new ArrayList<>();

		for (Veiculo veiculo : veiculos) {
			VagaVeiculosDTO dto = new VagaVeiculosDTO();
			dto.setPlaca(veiculo.getPlaca());
			dto.setCor(veiculo.getCor());
			dto.setModelo(veiculo.getModelo());

			Vaga vaga = veiculo.getVaga();
			if (vaga != null) {
				dto.setVaga(vaga.getId());
				dto.setNumero(vaga.getNumero());
				dto.setBloco(vaga.getBloco());
			}

			dtos.add(dto);
		}

		return dtos;
	}
}

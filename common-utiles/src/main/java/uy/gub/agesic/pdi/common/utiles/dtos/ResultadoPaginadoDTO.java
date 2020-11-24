package uy.gub.agesic.pdi.common.utiles.dtos;

import java.util.List;

public class ResultadoPaginadoDTO<T> extends BaseDTO {

	private static final long serialVersionUID = 1L;

	private Long totalTuplas;
	private List<T> resultado;
	
	public ResultadoPaginadoDTO() {
		this.totalTuplas = 0l;
		this.resultado = null;
	}

	public ResultadoPaginadoDTO(List<T> items) {
		this.totalTuplas = 0l;
		this.resultado = items;
	}

	public Long getTotalTuplas() {
		return totalTuplas;
	}

	public void setTotalTuplas(Long totalTuplas) {
		this.totalTuplas = totalTuplas;
	}

	public List<T> getResultado() {
		return resultado;
	}

	public void setResultado(List<T> resultado) {
		this.resultado = resultado;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resultado == null) ? 0 : resultado.hashCode());
		result = prime * result + ((totalTuplas == null) ? 0 : totalTuplas.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultadoPaginadoDTO other = (ResultadoPaginadoDTO) obj;
		if (resultado == null) {
			if (other.resultado != null)
				return false;
		} else if (!resultado.equals(other.resultado))
			return false;
		if (totalTuplas == null) {
			if (other.totalTuplas != null)
				return false;
		} else if (!totalTuplas.equals(other.totalTuplas))
			return false;
		return true;
	}
	
}

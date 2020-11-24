package uy.gub.agesic.pdi.common.utiles.dtos;

public class FiltroDTO extends BaseDTO {

	private Integer currentPage;
	private Integer pageSize;
	private Boolean calculateTotal;

	public FiltroDTO() {
		this.currentPage = 0;
		this.pageSize = 0;
		this.calculateTotal = false;
	}

	public FiltroDTO(Integer currentPage, Integer pageSize) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.calculateTotal = false;

		if (currentPage == null) {
			this.currentPage = 0;
		}
	}

	public FiltroDTO(Integer currentPage, Integer pageSize, Boolean calculateTotal) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.calculateTotal = calculateTotal;

		if (currentPage == null) {
			this.currentPage = 0;
		}
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public void setCalculateTotal(Boolean calculateTotal) {
		this.calculateTotal = calculateTotal;
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public Boolean getCalculateTotal() {
		return calculateTotal;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FiltroDTO filtroDTO = (FiltroDTO) o;

		if (currentPage != null ? !currentPage.equals(filtroDTO.currentPage) : filtroDTO.currentPage != null)
			return false;
		if (pageSize != null ? !pageSize.equals(filtroDTO.pageSize) : filtroDTO.pageSize != null) return false;
		return calculateTotal != null ? calculateTotal.equals(filtroDTO.calculateTotal) : filtroDTO.calculateTotal == null;
	}

	@Override
	public int hashCode() {
		int result = currentPage != null ? currentPage.hashCode() : 0;
		result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
		result = 31 * result + (calculateTotal != null ? calculateTotal.hashCode() : 0);
		return result;
	}
}

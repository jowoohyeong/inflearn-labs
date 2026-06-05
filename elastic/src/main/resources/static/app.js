document.addEventListener('DOMContentLoaded', () => {
  // DOM Elements
  const reindexReset = document.getElementById('reindex-reset');
  const reindexChunk = document.getElementById('reindex-chunk');
  const chunkSizeVal = document.getElementById('chunk-size-val');
  const btnReindex = document.getElementById('btn-reindex');
  
  const productForm = document.getElementById('product-form');
  const pName = document.getElementById('p-name');
  const pPrice = document.getElementById('p-price');
  const pRating = document.getElementById('p-rating');
  const pCategory = document.getElementById('p-category');
  const pDesc = document.getElementById('p-desc');
  
  const searchInput = document.getElementById('search-input');
  const suggestionsBox = document.getElementById('suggestions-box');
  const searchSource = document.getElementById('search-source');
  const filterCategory = document.getElementById('filter-category');
  const filterMinPrice = document.getElementById('filter-min-price');
  const filterMaxPrice = document.getElementById('filter-max-price');
  const btnSearch = document.getElementById('btn-search');
  const resultsGrid = document.getElementById('results-grid');
  const toastContainer = document.getElementById('toast-container');

  let debounceTimer;

  // 1. Toast alerts
  function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
      <span>${message}</span>
      <span style="cursor:pointer; margin-left: 10px; font-weight:bold;" onclick="this.parentElement.remove()">×</span>
    `;
    toastContainer.appendChild(toast);
    
    // Auto-remove after 4 seconds
    setTimeout(() => {
      toast.classList.add('fade-out');
      toast.addEventListener('animationend', () => toast.remove());
    }, 4000);
  }

  // 3. Update Chunk Size label
  reindexChunk.addEventListener('input', (e) => {
    chunkSizeVal.textContent = e.target.value;
  });

  // 4. Trigger Re-indexing
  btnReindex.addEventListener('click', async () => {
    const reset = reindexReset.checked;
    const chunk = reindexChunk.value;
    
    // Set loading state
    btnReindex.disabled = true;
    btnReindex.innerHTML = `<span class="spinner"></span> 색인 진행 중...`;

    try {
      const res = await fetch(`/products/reindex?reset=${reset}&chunkSize=${chunk}`, {
        method: 'POST'
      });
      if (res.ok) {
        const text = await res.text();
        showToast(text, 'success');
        fetchProducts(); // Refresh list after reindexing
      } else {
        throw new Error('재색인 요청에 실패했습니다.');
      }
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      btnReindex.disabled = false;
      btnReindex.innerHTML = `
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></polygon></svg>
        재색인 실행 (Sync DB ➡️ ES)
      `;
    }
  });

  // 5. Submit Product registration
  productForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      name: pName.value,
      price: parseInt(pPrice.value, 10),
      rating: parseFloat(pRating.value),
      category: pCategory.value,
      description: pDesc.value
    };

    try {
      const res = await fetch('/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        showToast('상품이 데이터베이스 및 검색 인덱스에 성공적으로 등록되었습니다!', 'success');
        productForm.reset();
        fetchProducts(); // refresh results list
      } else {
        throw new Error('상품 등록 실패');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // 6. Autocomplete suggestions
  searchInput.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    const query = searchInput.value.trim();
    const source = searchSource.value;

    if (source === 'rdb' || !query) {
      suggestionsBox.style.display = 'none';
      return;
    }

    debounceTimer = setTimeout(async () => {
      try {
        const res = await fetch(`/products/suggestions?query=${encodeURIComponent(query)}`);
        if (res.ok) {
          const suggestions = await res.json();
          if (suggestions && suggestions.length > 0) {
            suggestionsBox.innerHTML = suggestions
              .map(item => `<div class="suggestion-item">${escapeHtml(item)}</div>`)
              .join('');
            suggestionsBox.style.display = 'block';
            
            // Add click listener to suggestion items
            document.querySelectorAll('.suggestion-item').forEach(item => {
              item.addEventListener('click', () => {
                searchInput.value = item.textContent;
                suggestionsBox.style.display = 'none';
                fetchProducts(); // Trigger search
              });
            });
          } else {
            suggestionsBox.style.display = 'none';
          }
        }
      } catch (err) {
        console.error('자동완성 호출 오류', err);
      }
    }, 250);
  });

  // Close suggestion box on outside click
  document.addEventListener('click', (e) => {
    if (!searchInput.contains(e.target) && !suggestionsBox.contains(e.target)) {
      suggestionsBox.style.display = 'none';
    }
  });

  // Toggle search bar placeholders based on source
  searchSource.addEventListener('change', () => {
    const source = searchSource.value;
    if (source === 'rdb') {
      searchInput.placeholder = '전체 조회 모드입니다. 검색어를 무시하고 모든 DB 데이터를 가져옵니다.';
      searchInput.disabled = true;
      suggestionsBox.style.display = 'none';
    } else {
      searchInput.placeholder = '검색어를 입력해 보세요 (예: 삼성, 맥북, 가전...)';
      searchInput.disabled = false;
    }
    fetchProducts();
  });

  // Trigger search on click
  btnSearch.addEventListener('click', fetchProducts);
  
  // Also search on entering enter key
  searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      suggestionsBox.style.display = 'none';
      fetchProducts();
    }
  });

  // Helper to escape HTML tags to avoid XSS (except trusted highlight tags)
  function escapeHtml(text) {
    if (!text) return '';
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Helper to format prices
  function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  }

  // 7. Fetch Product list & render
  async function fetchProducts() {
    const source = searchSource.value;
    const query = searchInput.value.trim();
    const category = filterCategory.value.trim();
    const minPrice = filterMinPrice.value || 0;
    const maxPrice = filterMaxPrice.value || 100000000;

    resultsGrid.innerHTML = `
      <div class="no-results" style="border:none;">
        <span class="spinner" style="width:32px; height:32px; border-width:3px; border-top-color:var(--accent-purple);"></span>
        <p style="margin-top:0.5rem;">데이터 로딩 중...</p>
      </div>
    `;

    try {
      let url = '';
      if (source === 'rdb') {
        url = '/products?page=1&size=100';
      } else {
        url = `/products/search?query=${encodeURIComponent(query || ' ')}&minPrice=${minPrice}&maxPrice=${maxPrice}&page=1&size=50`;
        if (category) {
          url += `&category=${encodeURIComponent(category)}`;
        }
      }

      const res = await fetch(url);
      if (!res.ok) throw new Error('조회 요청 중 오류가 발생했습니다.');
      
      const data = await res.json();
      renderProducts(data, source);
    } catch (err) {
      showToast(err.message, 'error');
      resultsGrid.innerHTML = `
        <div class="no-results">
          <svg width="24" height="24" fill="none" stroke="var(--danger)" stroke-width="2"><path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
          <p>데이터를 불러오지 못했습니다.</p>
          <span style="font-size:0.85rem; color:var(--text-secondary);">스프링 서버 및 데이터베이스 구동 여부를 확인해 주세요.</span>
        </div>
      `;
    }
  }

  // 8. Render cards
  function renderProducts(products, source) {
    if (!products || products.length === 0) {
      resultsGrid.innerHTML = `
        <div class="no-results">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <p>조회 또는 검색 결과가 존재하지 않습니다.</p>
        </div>
      `;
      return;
    }

    resultsGrid.innerHTML = products.map(product => {
      const id = product.id;
      // In ES, title can contain <b> tag from highlight query. Since it's from ES, we can trust the <b> tag
      // For general cases, let's treat name with caution, but render <b> for highlighted matches.
      const nameHtml = source === 'es' ? product.name : escapeHtml(product.name);
      const category = escapeHtml(product.category);
      const description = escapeHtml(product.description);
      const rating = product.rating ? product.rating.toFixed(1) : '0.0';
      const price = formatNumber(product.price);

      return `
        <div class="product-card" data-id="${id}">
          <div>
            <div class="product-header">
              <span class="product-category">${category}</span>
              <span class="product-rating">★ ${rating}</span>
            </div>
            <h3 class="product-title">${nameHtml}</h3>
            <p class="product-desc" title="${description}">${description}</p>
          </div>
          <div class="product-footer">
            <span class="product-price">${price}</span>
            <button class="btn-danger btn-delete" data-id="${id}">삭제</button>
          </div>
        </div>
      `;
    }).join('');

    // Attach click listeners to Delete buttons
    document.querySelectorAll('.btn-delete').forEach(button => {
      button.addEventListener('click', async (e) => {
        const id = e.target.getAttribute('data-id');
        if (confirm('이 상품을 삭제하시겠습니까? (MySQL 및 Elasticsearch에서 함께 제거됩니다)')) {
          try {
            const res = await fetch(`/products/${id}`, { method: 'DELETE' });
            if (res.ok) {
              showToast('상품이 정상적으로 삭제되었습니다.', 'success');
              fetchProducts(); // Reload list
            } else {
              throw new Error('상품 삭제에 실패했습니다.');
            }
          } catch (err) {
            showToast(err.message, 'error');
          }
        }
      });
    });
  }

  // Initialization
  fetchProducts();
});

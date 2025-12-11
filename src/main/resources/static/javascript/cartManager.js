// cartManager.js - Server-backed Cart Manager (DTO format A)
// Expected server responses:
// GET  /api/carts/me  -> { items: [{id, productId, productName, price, quantity}], total: number }
// POST /api/carts/items?productId= -> returns added item or 200
// PATCH/DELETE endpoints as used below.
//
// Always uses credentials: 'same-origin' so Spring session cookies are sent.

class CartManager {
    constructor() {
        this.cart = { items: [], total: 0 };
        // Try to load immediately
        this._ready = this.refreshFromServer().catch(() => {});
    }

    // Public: ensure initial load finished
    async ready() {
        return this._ready;
    }

    async refreshFromServer() {
        try {
            const resp = await fetch('/api/carts/me', { method: 'GET', credentials: 'same-origin' });
            if (!resp.ok) {
                // if server returns 401 or 403, keep cart empty
                console.warn('Failed to load cart from server', resp.status);
                this.cart = { items: [], total: 0 };
                window.dispatchEvent(new CustomEvent('cartUpdated', { detail: this.cart }));
                return this.cart;
            }
            const data = await resp.json();
            // Normalize fields just in case
            this.cart = {
                items: Array.isArray(data.items) ? data.items : [],
                total: (typeof data.total === 'number') ? data.total : (Array.isArray(data.items) ? data.items.reduce((s,i)=>s + (i.price * i.quantity),0) : 0)
            };
            window.dispatchEvent(new CustomEvent('cartUpdated', { detail: this.cart }));
            return this.cart;
        } catch (e) {
            console.error('Error loading cart from server', e);
            this.cart = { items: [], total: 0 };
            window.dispatchEvent(new CustomEvent('cartUpdated', { detail: this.cart }));
            return this.cart;
        }
    }

    async addItem(productId, quantity = 1) {
        try {
            const resp = await fetch(`/api/carts/items?productId=${encodeURIComponent(productId)}&quantity=${encodeURIComponent(quantity)}`, {
                method: 'POST',
                credentials: 'same-origin'
            });
            if (!resp.ok) {
                const err = await resp.json().catch(()=>null);
                throw new Error(err && err.error ? err.error : ('HTTP ' + resp.status));
            }
            await this.refreshFromServer();
        } catch (e) {
            console.error('addItem error', e);
            throw e;
        }
    }

    async increase(itemId) {
        const resp = await fetch(`/api/carts/items/${itemId}/increase`, { method: 'PATCH', credentials: 'same-origin' });
        if (!resp.ok) throw new Error('HTTP ' + resp.status);
        await this.refreshFromServer();
    }

    async decrease(itemId) {
        const resp = await fetch(`/api/carts/items/${itemId}/decrease`, { method: 'PATCH', credentials: 'same-origin' });
        if (!resp.ok) throw new Error('HTTP ' + resp.status);
        await this.refreshFromServer();
    }

    async remove(itemId) {
        const resp = await fetch(`/api/carts/items/${itemId}`, { method: 'DELETE', credentials: 'same-origin' });
        if (!resp.ok) throw new Error('HTTP ' + resp.status);
        await this.refreshFromServer();
    }

    // Utilities used by frontend
    getItems() {
        return this.cart.items || [];
    }

    getTotal() {
        return this.cart.total || 0;
    }

    // Number of unique positions (variant 2)
    getUniqueCount() {
        return (this.cart.items || []).length;
    }
}

// expose singleton
window.cartManager = new CartManager();

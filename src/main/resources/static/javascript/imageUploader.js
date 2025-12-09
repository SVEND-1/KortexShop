//imageUploader.js
// Обработчик загрузки изображений


class ImageUploader {
    constructor() {
        this.storageKey = 'productImages';
        this.loadImages();
    }

    loadImages() {
        const saved = localStorage.getItem(this.storageKey);
        this.images = saved ? JSON.parse(saved) : {};
    }

    saveImages() {
        localStorage.setItem(this.storageKey, JSON.stringify(this.images));
    }

    // Сохранение изображения из файла
    saveImageFromFile(productId, file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            
            reader.onload = (e) => {
                this.images[productId] = e.target.result;
                this.saveImages();
                resolve(e.target.result);
            };
            
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
    }

    // Получение изображения по ID товара
    getImage(productId) {
        return this.images[productId] || 'images/product-img.png';
    }

    // Удаление изображения
    removeImage(productId) {
        delete this.images[productId];
        this.saveImages();
    }
}

window.imageUploader = new ImageUploader();
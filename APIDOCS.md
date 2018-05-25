Build
`mvn compile && mvn package && asadmin deploy --force=true target/jazzad6.war`

Kategorie produktów
- GRAPHIC_CARD
- MEMORY
- MOTHERBOARD
- HARD_DISK

API
Lista produktów
GET /v1/products

Produkt po ID
GET /v1/products/1

Dodanie produktu
POST /v1/products
```json
{
    "name": "ASUS WS C621E SAGE",
    "price": 2299.99,
    "category": "MOTHERBOARD"
}
```

Aktualizacja produktu
PUT /v1/products/1
```json
{
    "name": "Nvidia GeForce GTX1070",
    "price": 2999.99,
    "category": "GRAPHIC_CARD"
}
```

Wyświetlenie komentarzy dla produktu
GET /v1/products/1/comments

Dodanie komentarza dla produktu
POST /v1/products/1/comments
```json
{
    "author": "Anonim",
    "content": "Great!!!"
}
```

Usunięcie komentarza
DELETE /v1/products/1/comments/1

Wyszukiwanie produktów
GET /v1/products/search?category=GRAPHIC_CARD&name=GTX&priceFrom=1000&priceTo=4000
- category = GRAPHIC_CARD || MEMORY || MOTHERBOARD || HARD_DISK
- name - wyszukiwanie z użyciem LIKE %name%
- priceFrom - cena od (double, int też działa)
- priceTo - cena do (double, int też działa)

// carousel.js
// Dynamically loads 15-20 images promoting Indian elections, voting, culture, and nationality into the carousel.

const carouselImages = [];

const carousel = document.getElementById('carousel');
let currentIndex = 0;

function renderCarousel() {
  carousel.innerHTML = '';
  const img = document.createElement('img');
  img.src = carouselImages[currentIndex];
  img.alt = 'Indian Election, Voting, or Culture';
  img.style.width = '100%';
  img.style.borderRadius = '12px';
  img.style.boxShadow = '0 2px 12px rgba(0,0,0,0.12)';
  img.style.maxHeight = '340px';
  img.style.objectFit = 'cover';
  carousel.appendChild(img);
}

function showPrev() {
  currentIndex = (currentIndex - 1 + carouselImages.length) % carouselImages.length;
  renderCarousel();
}

function showNext() {
  currentIndex = (currentIndex + 1) % carouselImages.length;
  renderCarousel();
}

document.addEventListener('DOMContentLoaded', () => {
  renderCarousel();
  document.querySelector('.carousel-btn.prev').addEventListener('click', showPrev);
  document.querySelector('.carousel-btn.next').addEventListener('click', showNext);
});

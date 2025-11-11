import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dev-info',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dev-info-container" *ngIf="showInfo">
      <div class="dev-info-card">
        <button class="close-btn" (click)="hideInfo()">✕</button>
        
        <div class="dev-header">
          <h3>🚀 Musify - Características Implementadas</h3>
          <p class="subtitle">Proyecto académico TFU Unidad 4</p>
        </div>

        <div class="features-grid">
          <!-- Performance Features -->
          <div class="feature-category">
            <h4>⚡ Performance (TTP < 800ms)</h4>
            <ul>
              <li>✅ Reproductor con precarga de audio</li>
              <li>✅ Cache de búsquedas y recomendaciones</li>
              <li>✅ Lazy loading de componentes</li>
              <li>✅ Operaciones asíncronas optimizadas</li>
            </ul>
          </div>

          <!-- Player Logic -->
          <div class="feature-category">
            <h4>🎵 Lógica del Reproductor</h4>
            <ul>
              <li>✅ PlayNext/PlayPrevious con cola</li>
              <li>✅ Autoplay con onTrackEnd</li>
              <li>✅ Shuffle y Repeat modes</li>
              <li>✅ Control de volumen y progreso</li>
            </ul>
          </div>

          <!-- Search API -->
          <div class="feature-category">
            <h4>🔍 API de Búsqueda</h4>
            <ul>
              <li>✅ Búsqueda categorizada (JSON estructurado)</li>
              <li>✅ Filtros por canciones/álbumes/artistas/conciertos</li>
              <li>✅ Integración con Spotify API</li>
              <li>✅ Endpoints REST + SOAP/XML</li>
            </ul>
          </div>

          <!-- UI/UX -->
          <div class="feature-category">
            <h4>🎨 Frontend UI/UX</h4>
            <ul>
              <li>✅ Diseño responsive y simétrico</li>
              <li>✅ Componentes modulares Angular</li>
              <li>✅ Modo búsqueda simple/avanzada</li>
              <li>✅ Interfaz intuitiva y moderna</li>
            </ul>
          </div>

          <!-- Authentication -->
          <div class="feature-category">
            <h4>🔐 Autenticación</h4>
            <ul>
              <li>✅ JWT con logout mejorado</li>
              <li>✅ Menú de usuario integrado</li>
              <li>✅ Guard de autenticación</li>
              <li>✅ Gestión de estado reactivo</li>
            </ul>
          </div>

          <!-- Architecture -->
          <div class="feature-category">
            <h4>🏗️ Arquitectura</h4>
            <ul>
              <li>✅ Patrones de resiliencia (Circuit Breaker, Retry)</li>
              <li>✅ Cache-Aside para performance</li>
              <li>✅ Async Request-Reply</li>
              <li>✅ Deployment con Docker + NGINX</li>
            </ul>
          </div>
        </div>

        <div class="tech-stack">
          <h4>🛠️ Stack Tecnológico</h4>
          <div class="tech-badges">
            <span class="tech-badge backend">Spring Boot 3</span>
            <span class="tech-badge frontend">Angular 17</span>
            <span class="tech-badge database">PostgreSQL</span>
            <span class="tech-badge integration">Spotify API</span>
            <span class="tech-badge integration">YouTube API</span>
            <span class="tech-badge deployment">Docker</span>
            <span class="tech-badge deployment">NGINX</span>
            <span class="tech-badge patterns">Resilience4j</span>
          </div>
        </div>

        <div class="dev-footer">
          <p>🎓 Desarrollo académico - Patrones de Arquitectura de Software</p>
          <small>Implementación completa de tácticas de disponibilidad, performance, seguridad y modificabilidad</small>
        </div>
      </div>
    </div>

    <!-- Toggle Button -->
    <button class="dev-toggle-btn" (click)="toggleInfo()" [class.active]="showInfo">
      <span *ngIf="!showInfo">ℹ️</span>
      <span *ngIf="showInfo">📋</span>
    </button>
  `,
  styles: [`
    .dev-info-container {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.8);
      backdrop-filter: blur(5px);
      z-index: 1000;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
      animation: fadeIn 0.3s ease;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    .dev-info-card {
      background: linear-gradient(135deg, #1a1a2e, #16213e);
      border-radius: 20px;
      padding: 30px;
      max-width: 900px;
      max-height: 80vh;
      overflow-y: auto;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
      position: relative;
      animation: slideUp 0.3s ease;
    }

    @keyframes slideUp {
      from { transform: translateY(50px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }

    .close-btn {
      position: absolute;
      top: 15px;
      right: 20px;
      background: rgba(255, 255, 255, 0.1);
      border: none;
      color: white;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      cursor: pointer;
      font-size: 16px;
      transition: all 0.3s ease;
    }

    .close-btn:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: scale(1.1);
    }

    .dev-header {
      text-align: center;
      margin-bottom: 30px;
    }

    .dev-header h3 {
      color: white;
      font-size: 1.8em;
      margin-bottom: 8px;
    }

    .subtitle {
      color: #4CAF50;
      font-size: 1.1em;
      margin: 0;
    }

    .features-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 20px;
      margin-bottom: 30px;
    }

    .feature-category {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      padding: 20px;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .feature-category h4 {
      color: #4CAF50;
      font-size: 1.1em;
      margin-bottom: 15px;
      border-bottom: 1px solid rgba(76, 175, 80, 0.3);
      padding-bottom: 8px;
    }

    .feature-category ul {
      list-style: none;
      padding: 0;
      margin: 0;
    }

    .feature-category li {
      color: #ccc;
      padding: 4px 0;
      font-size: 0.9em;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .tech-stack {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 20px;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .tech-stack h4 {
      color: #2196F3;
      margin-bottom: 15px;
    }

    .tech-badges {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .tech-badge {
      padding: 6px 12px;
      border-radius: 15px;
      font-size: 0.8em;
      font-weight: 500;
      color: white;
    }

    .tech-badge.backend {
      background: linear-gradient(135deg, #4CAF50, #45a049);
    }

    .tech-badge.frontend {
      background: linear-gradient(135deg, #2196F3, #1976d2);
    }

    .tech-badge.database {
      background: linear-gradient(135deg, #FF9800, #f57c00);
    }

    .tech-badge.integration {
      background: linear-gradient(135deg, #9C27B0, #7b1fa2);
    }

    .tech-badge.deployment {
      background: linear-gradient(135deg, #607D8B, #455a64);
    }

    .tech-badge.patterns {
      background: linear-gradient(135deg, #FF5722, #d84315);
    }

    .dev-footer {
      text-align: center;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      padding-top: 20px;
    }

    .dev-footer p {
      color: white;
      margin-bottom: 8px;
      font-weight: 500;
    }

    .dev-footer small {
      color: #999;
      font-size: 0.85em;
    }

    .dev-toggle-btn {
      position: fixed;
      bottom: 20px;
      right: 20px;
      width: 50px;
      height: 50px;
      border-radius: 50%;
      background: linear-gradient(135deg, #4CAF50, #2196F3);
      border: none;
      color: white;
      font-size: 1.2em;
      cursor: pointer;
      z-index: 999;
      transition: all 0.3s ease;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
    }

    .dev-toggle-btn:hover {
      transform: scale(1.1);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4);
    }

    .dev-toggle-btn.active {
      background: linear-gradient(135deg, #FF5722, #f44336);
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .dev-info-card {
        padding: 20px;
        margin: 10px;
      }

      .features-grid {
        grid-template-columns: 1fr;
        gap: 15px;
      }

      .dev-header h3 {
        font-size: 1.5em;
      }

      .tech-badges {
        justify-content: center;
      }

      .dev-toggle-btn {
        bottom: 90px;
        right: 15px;
        width: 45px;
        height: 45px;
      }
    }

    /* Custom Scrollbar */
    .dev-info-card::-webkit-scrollbar {
      width: 8px;
    }

    .dev-info-card::-webkit-scrollbar-track {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 4px;
    }

    .dev-info-card::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.3);
      border-radius: 4px;
    }

    .dev-info-card::-webkit-scrollbar-thumb:hover {
      background: rgba(255, 255, 255, 0.5);
    }
  `]
})
export class DevInfoComponent {
  showInfo: boolean = false;

  toggleInfo(): void {
    this.showInfo = !this.showInfo;
  }

  hideInfo(): void {
    this.showInfo = false;
  }
}
import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  // Cambia el appId antes de publicar: formato inverso de dominio, único en las stores.
  appId: 'com.quantumfinance.bolsillo',
  appName: 'Bolsillo',
  webDir: 'www',

  // No usamos live-reload por defecto: los assets van embebidos en el APK/IPA.
  // Descomenta "server" solo durante desarrollo si quieres apuntar al bundler/host local.
  // server: {
  //   url: 'http://192.168.1.50:5173',
  //   cleartext: true
  // },

  plugins: {
    SplashScreen: {
      launchShowDuration: 800,
      launchAutoHide: true,
      backgroundColor: '#020205',
      androidSplashResourceName: 'splash',
      androidScaleType: 'CENTER_CROP',
      showSpinner: false,
      splashFullScreen: true,
      splashImmersive: true
    },
    StatusBar: {
      style: 'DARK',
      backgroundColor: '#020205',
      overlaysWebView: false
    },
    LocalNotifications: {
      smallIcon: 'ic_stat_icon',
      iconColor: '#6366f1'
    }
  },

  android: {
    allowMixedContent: false
  },

  ios: {
    contentInset: 'automatic'
  }
};

export default config;

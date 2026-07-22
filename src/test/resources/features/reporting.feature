# language: fr
Fonctionnalité: Reporting

  Scénario: Obtenir le reporting global
    Quand je consulte le reporting global via l'API
    Alors la reponse de reporting global est 200

  Scénario: Obtenir le reporting qualite
    Quand je consulte le reporting qualite entre "2026-01-01T00:00:00" et "2026-12-31T23:59:59"
    Alors la reponse de reporting qualite est 200

  Scénario: Obtenir le reporting stock
    Quand je consulte le reporting stock entre "2026-01-01T00:00:00" et "2026-12-31T23:59:59"
    Alors la reponse de reporting stock est 200


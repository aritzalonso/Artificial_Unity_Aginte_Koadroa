"""Egiaztapen-tresna: aplikazioaren CSV-ak (--esportatu) eta sql/aginte_kontsultak.sql-en emaitzak alderatzen ditu.

Erabilera:  python egiaztapena/alderatu.py <sql_emaitza.txt-ren karpeta> <CSV karpeta> <AAAA-HH-EE>
sql_emaitza.txt Egiaztapena.java-k sortzen du; CSV-ak: java -jar target/aginte-koadroa.jar --esportatu=<karpeta>
"""
import csv
import re
import sys
from decimal import Decimal
from pathlib import Path

d = Path(sys.argv[1])          # egiaztapena karpeta (sql_emaitza.txt)
e = Path(sys.argv[2])          # esportazio-karpeta (CSV-ak)
data = sys.argv[3]

# SQL emaitzak ataletan banatu
atalak, unekoa = {}, None
for lerroa in (d / 'sql_emaitza.txt').read_text(encoding='utf-8-sig').splitlines():
    if lerroa.startswith('=== '):
        unekoa = lerroa.split('.')[0].replace('=== ', '').strip()
        atalak[unekoa] = []
    elif unekoa and lerroa.strip():
        atalak[unekoa].append([x.strip() for x in lerroa.split(' | ')])


def sql(zk):
    goiburua, *lerroak = atalak[str(zk)]
    return [dict(zip(goiburua, l)) for l in lerroak]


def csvak(izena):
    with open(e / f'{izena}_{data}.csv', encoding='utf-8-sig', newline='') as f:
        return list(csv.DictReader(f, delimiter=';'))


def zenb(s):
    return Decimal(s.replace(',', '.')) if s not in ('', 'null', None) else None


akatsak = 0


def berdin(izena, a, b):
    global akatsak
    ok = (zenb(a) == zenb(b)) if re.fullmatch(r'-?\d+([.,]\d+)?', a or '') and re.fullmatch(r'-?\d+([.,]\d+)?', b or '') else (a == b)
    if not ok:
        akatsak += 1
        print(f'  DESBERDIN {izena}: app={a!r} sql={b!r}')
    return ok


def egiaztatu(izenburua, app, sqlr, parekak):
    print(f'{izenburua}: app {len(app)} lerro, SQL {len(sqlr)} lerro')
    global akatsak
    if len(app) != len(sqlr):
        akatsak += 1
        print('  LERRO KOPURU DESBERDINA')
    for a, s in zip(app, sqlr):
        for ca, cs, f in parekak:
            berdin(ca, a[ca], f(s[cs]) if f else s[cs])


iso2es = lambda x: '' if x in ('', 'null') else f'{x[8:10]}/{x[5:7]}/{x[0:4]}'
es = lambda x: x.replace('.', ',') if x not in ('null',) else ''
bool_ = lambda x: 'Bai' if x == 't' else 'Ez'

# 1. Laburpena
lab = {r['Adierazlea']: r for r in csvak('laburpena')}
s1 = sql(1)[0]
print('Laburpena:')
berdin('salmentak', lab['Salmentak guztira (BEZik gabe, €)']['Aukeratutako aldia'], es(s1['salmentak_guztira']))
berdin('eskaerak', lab['Salmenta-eskaerak']['Aukeratutako aldia'], s1['eskaerak'])
berdin('batez bestekoa', lab['Batez besteko eskaera (€)']['Aukeratutako aldia'], es(s1['batez_besteko_eskaera']))
berdin('bezeroak', lab['Bezero desberdinak']['Aukeratutako aldia'], s1['bezeroak'])
berdin('kobratzeko', lab['Kobratzeko zain gaur (BEZarekin, €)']['Aukeratutako aldia'], es(s1['kobratzeko_zain']))

hil = [r for r in csvak('salmentak_hileka') if r['Mota'] == 'Benetakoa']
egiaztatu('Hileko salmentak', hil, sql(2), [('Hilabetea', 'hilabetea', None), ('Salmentak (BEZik gabe)', 'salmentak', es), ('Eskaerak', 'eskaerak', None)])
egiaztatu('Produktu salduenak', csvak('produktu_salduenak'), sql(3), [('Erreferentzia', 'erreferentzia', None), ('Produktua', 'produktua', None), ('Unitateak', 'unitateak', es), ('Salmentak (BEZik gabe)', 'zenbatekoa', es)])
egiaztatu('Bezeroak', csvak('bezeroak'), sql(4), [('Bezeroa', 'bezeroa', None), ('Herria', 'herria', None), ('Eskaerak', 'eskaerak', None), ('Salmentak (BEZik gabe)', 'salmentak', es), ('Azken eskaera', 'azken_eskaera', iso2es)])
egiaztatu('Stocka', csvak('stocka'), sql(5), [('Erreferentzia', 'erreferentzia', None), ('Stocka', 'stocka', es), ('Minimoa', 'minimoa', es), ('Maximoa', 'maximoa', es), ('Stock baxua', 'stock_baxua', bool_)])
egiaztatu('Fakturak', csvak('kobratzeko_fakturak'), sql(6), [('Faktura', 'faktura', None), ('Bezeroa', 'bezeroa', None), ('Data', 'data', iso2es), ('Muga-eguna', 'muga_eguna', iso2es), ('Guztira', 'guztira_bezarekin', es), ('Kobratzeko', 'ordaintzeko', es), ('Egun berandu', 'berandu_egunak', None)])
egiaztatu('Kategoriak', csvak('kategoriak'), sql(7), [('Kategoria', 'kategoria', None), ('Unitateak', 'unitateak', es), ('Salmentak (BEZik gabe)', 'zenbatekoa', es)])
print('EMAITZA:', 'DENA BAT DATOR' if akatsak == 0 else f'{akatsak} desberdintasun')

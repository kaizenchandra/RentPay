#!/usr/bin/env python3
"""Validate source ID/contract/alias coverage. Never infer implementation from a link."""
import re
from pathlib import Path
source=Path('lmt-full-stack-prompt.md').read_text()
index=source.split('### Requirement-to-Contract Audit Index\n',1)[1].split('### Remaining Decisions',1)[0]
contracts={fs:re.findall(r'`([A-Z][A-Z-]+-\d{3})`',ids) for fs,ids in re.findall(r'\| (FS-\d{2}) \| (.*?) \|',index)}
ids=set(x for values in contracts.values() for x in values)
assert len(ids)==195, f'Expected 195 functional IDs, found {len(ids)}'
assert len(contracts)==36
aliases=[]
for group,start,end in re.findall(r'FR-(\d{2})\.(\d{2})–FR-\d{2}\.(\d{2})',source):
    aliases.extend(f'FR-{group}.{n:02}' for n in range(int(start),int(end)+1))
aliases=sorted(set(aliases))+['FR-03']
def anchor(heading):
    return re.sub(r'[^\w\- ]','',heading.lower()).replace(' ','-')
fs_blocks={}
for match in re.finditer(r'^### (FS-\d{2}) — (.+)$',source,re.M):
    end=source.find('\n### ',match.end())
    fs_blocks[match[1]]=(match[2],source[match.end():end])
# Default phase owners are sequencing assignments, not completion claims.
fs_phases=['1','1','1','7','1','7','2','2','2','3','3','3','3','4','4','5','5','6','6','2','6','8','3','7A','7A','7A','7A','6','6','8','3','9','3','9','7A','9']
fs_owners=['identity','workspace','commercial','commercial','entitlements','platformadmin','property','authorization','inventory','discovery','leads','appointments','applications','tenancy','agreements','rentalfinance','rentalfinance','settlement','helpdesk','reporting','documents','communications','media','consumer','consumerexperience','services','sales','police','reviews','advertising','marketplace','jobs','marketplace','reporting','ai','ai']
if __name__=='__main__':
    import sys
    if '--generate' in sys.argv:
        if Path('docs/requirements-matrix.md').exists():
            raise SystemExit('Refusing to overwrite implementation evidence. Update the existing matrix and run this script without --generate.')
        lines=['# Requirement implementation matrix','',
        'Source: [unchanged full specification](../lmt-full-stack-prompt.md). This is an implementation inventory, not a completion claim. All 195 IDs retain their source business rules. FS references supply planned actors, inputs, outputs, permissions, transitions, notifications, recovery and acceptance criteria; actual routes, migrations and test evidence are recorded separately. Phase assignments below are initial sequencing and include cross-phase dependencies. Must unless the source explicitly qualifies a sub-capability; conditional provider enhancements are not silently dropped.','',
        'The public catalogue subset is Partial for SAAS-PLAN-001–003. No functional requirement is fully Implemented. Every other frontend/backend entry is Pending. There are no simulated provider integrations.','',
        '## Contract ownership and phase assignments','','| Contract | Owner | Phase | Source contract / acceptance | Runtime/store |','|---|---|---|---|---|']
        for n,(fs,(title,block)) in enumerate(fs_blocks.items()):
            lines.append(f'| {fs} | {fs_owners[n]} | {fs_phases[n]} | [{title}](../lmt-full-stack-prompt.md#fs-{n+1:02}) | Planned Java / PostgreSQL; Next.js UI |')
        lines+=['','## Functional requirements','','| ID / source objective | FS | Phase | Owner | Frontend | Backend | Actual evidence |','|---|---|---|---|---|---|---|']
        for id in sorted(ids):
            linked=[fs for fs,values in contracts.items() if id in values]
            heading=re.search(r'^### '+re.escape(id)+r' — (.+)$',source,re.M)
            title=heading[1] if heading else id
            phases=sorted(set(fs_phases[int(fs[3:])-1] for fs in linked),key=lambda s: float(s.replace('A','.5')))
            owners=sorted(set(fs_owners[int(fs[3:])-1] for fs in linked))
            partial=id.startswith('SAAS-PLAN-')
            evidence='[Catalogue delivery](release-status.md); /pricing; GET public/plans; V1; CatalogIntegrationTest; catalog.spec.ts' if partial else 'No code/test evidence; source-linked acceptance pending'
            lines.append(f'| [{id} — {title}](../lmt-full-stack-prompt.md#{anchor(id+" — "+title)}) | {", ".join(linked)} | {", ".join(phases)} | {", ".join(owners)} | {"Partial" if partial else "Pending"} | {"Partial" if partial else "Pending"} | {evidence} |')
        lines+=['','## Source aliases','','Inclusive aliases preserved below. The supplied source contains a family-level crosswalk, not child-level acceptance text. Each alias is linked to its adopted atomic catalogue anchor; detailed child interpretation/priority is unresolved until the original child text is supplied or located. These entries do not invent unseen acceptance rules or create duplicate implementations. FR-03 has an attribute table and no supplied child IDs.','', '| Alias | Atomic destination anchor | Priority / interpretation / conditionality | Phase | Evidence |','|---|---|---|---|---|']
        dest={1:'MARKET-001',2:'MARKET-002',3:'MARKET-002',4:'MARKET-003',5:'MARKET-003',6:'MARKET-006',7:'MARKET-005',8:'MARKET-005',9:'MARKET-004',10:'MARKET-008',11:'MARKET-009',12:'MARKET-013',13:'MARKET-014',14:'MARKET-013',15:'OCC-WF-002',16:'MARKET-011',17:'MARKET-012',18:'MARKET-001',19:'MARKET-015',20:'MARKET-015'}
        for alias in aliases:
            d=dest[int(alias[3:5])]
            phases=sorted(set(fs_phases[int(fs[3:])-1] for fs,values in contracts.items() if d in values))
            lines.append(f'| {alias} | {d} | Source family crosswalk retained; child interpretation Pending (DEC-002) | {", ".join(phases)} | Pending |')
        lines+=['','## Architecture and integration gates','','| Gate | Status | Evidence |','|---|---|---|']
        for prefix,count in [('ARCH',14),('INT',12)]:
            for n in range(1,count+1):
                lines.append(f'| {prefix}-{n:02} | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |')
        lines+=['','## Per-requirement delivery record','', 'Before implementing each Pending row, resolve its linked FS narrative and specific source block into concrete route/data/form validation, interaction, complete operation/DTO/errors, table ownership, permissions/entitlements/quota, transition guards, audit/outbox, notification recipients, recovery and negative-path acceptance tests. These details are currently source-defined designs, not implemented bindings. Do not advance status from this matrix alone.','']
        Path('docs/requirements-matrix.md').write_text('\n'.join(lines))
    matrix=Path('docs/requirements-matrix.md').read_text()
    for id in ids:
        assert re.search(r'\| \['+re.escape(id)+r' — ',matrix), f'Missing {id}'
    for fs,values in contracts.items():
        for id in values:
            row=next(line for line in matrix.splitlines() if line.startswith('| ['+id+' — '))
            assert fs in row, f'Missing mapping {id} -> {fs}'
    for alias in aliases: assert f'| {alias} |' in matrix, f'Missing alias {alias}'
    for prefix,count in [('ARCH',14),('INT',12)]:
        for n in range(1,count+1): assert f'| {prefix}-{n:02} |' in matrix
    print(f'PASS: {len(ids)} functional IDs, {len(contracts)} FS contracts, {len(aliases)} aliases, 14 ARCH and 12 INT gates')

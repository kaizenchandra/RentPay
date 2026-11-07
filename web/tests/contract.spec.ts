import {test,expect} from '@playwright/test';
import Ajv2020 from 'ajv/dist/2020';
import addFormats from 'ajv-formats';
import contract from '../../contracts/openapi/lmt-v1.json';
const ajv = new Ajv2020({strict:false});
addFormats(ajv);
function validate(name:string, body:unknown) {
  const check=ajv.compile({$ref:`#/components/schemas/${name}`,components:contract.components});
  expect(check(body),JSON.stringify(check.errors)).toBe(true);
}
test('live public list, detail and field errors conform to OpenAPI',async({request})=>{
  for (const interval of ['monthly','annual']) {
    const response=await request.get(`/api/v1/public/plans?interval=${interval}`);
    expect(response.status()).toBe(200);
    validate('Catalog',await response.json());
    const detail=await request.get(`/api/v1/public/plans/FREE?interval=${interval}`);
    expect(detail.status()).toBe(200);
    validate('Plan',await detail.json());
  }
  for (const [path,status] of [['/public/plans?audience=invalid',422],['/public/plans/UNKNOWN',404],['/admin/plans',401]] as const){
    const response=await request.get('/api/v1'+path);
    expect(response.status()).toBe(status);
    validate('Problem',await response.json());
  }
});
